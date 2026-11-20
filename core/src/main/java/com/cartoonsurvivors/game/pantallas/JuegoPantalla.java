package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.sistemas.GestorEnemigos;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;

public class JuegoPantalla extends ScreenAdapter {

    private static final float INTERVALO_MOVIMIENTO = 0.05f;

    private final CartoonSurvivors game;
    private final ClienteUDP clienteUDP;
    private final SpriteBatch batch;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);
    private final ExtendViewport hudViewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, hudCamera);
    private final ControladorEntrada controladorEntrada = new ControladorEntrada();
    private final TiledMap mapa;
    private final OrthogonalTiledMapRenderer renderizadorMapa;
    private final Jugador jugadorLocal;
    private final Jugador jugadorRemoto;
    private final SincronizadorJugadores sincronizadorJugadores;
    private final GestorEnemigos gestorEnemigosVisual;
    private final HUDJuego hudJuego;
    private final int idJugadorLocal;
    private float tiempoEnvio = INTERVALO_MOVIMIENTO;

    public JuegoPantalla(CartoonSurvivors game, Jugador jugador) {

        this.game = game;
        this.clienteUDP = game.getClienteUDP();
        this.batch = game.getBatch();

        jugadorLocal = jugador;

        // Por ahora el personaje remoto es Mordecai.
        jugadorRemoto = TipoPersonaje.MORDECAI.crearPersonaje();

        idJugadorLocal = clienteUDP.getIdJugador();

        mapa = new TmxMapLoader().load("mapas/mapa1.tmx");
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa);

        sincronizadorJugadores = new SincronizadorJugadores(jugadorLocal, jugadorRemoto, idJugadorLocal);

        gestorEnemigosVisual = new GestorEnemigos();

        hudJuego = new HUDJuego(batch, game.getFont(), hudViewport);
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(null);

        jugadorLocal.setPosicion(idJugadorLocal == 1 ? 6340 : 6460, 6400);

        jugadorRemoto.setPosicion(idJugadorLocal == 1 ? 6460 : 6340, 6400);

        camaraSeguirJugador();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            volverAlMenu();
            return;
        }

        if (clienteUDP.estaServidorDesconectado() || clienteUDP.estaSesionFinalizada() || !clienteUDP.estaPartidaIniciada()) {

            hudJuego.dibujarSesionFinalizada(clienteUDP.estaServidorDesconectado());

            return;
        }

        enviarMovimiento(delta);
        actualizarEstados();

        jugadorLocal.getArma().actualizar(delta);
        jugadorRemoto.getArma().actualizar(delta);

        camaraSeguirJugador();
        dibujarMundo(delta);

        hudJuego.dibujarMultijugador(jugadorLocal, jugadorRemoto, idJugadorLocal, sincronizadorJugadores.hayEstadoRecibido());
    }

    private void enviarMovimiento(float delta) {

        tiempoEnvio += delta;

        if (tiempoEnvio < INTERVALO_MOVIMIENTO) {
            return;
        }

        tiempoEnvio = 0;

        float direccionX = controladorEntrada.obtenerDireccionX();

        float direccionY = controladorEntrada.obtenerDireccionY();

        clienteUDP.enviarMovimiento(direccionX, direccionY);
    }

    private void actualizarEstados() {

        sincronizadorJugadores.procesarEstado(clienteUDP.consumirUltimoEstado());

        gestorEnemigosVisual.procesarEstado(clienteUDP.consumirEstadoEnemigos());
    }

    private void camaraSeguirJugador() {

        camera.position.set(jugadorLocal.getPosicionX() + 20, jugadorLocal.getPosicionY() + 30, 0);

        camera.update();
    }

    private void dibujarMundo(float delta) {

        viewport.apply();

        renderizadorMapa.setView(camera);
        renderizadorMapa.render();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        jugadorLocal.dibujar(batch, delta);
        jugadorRemoto.dibujar(batch, delta);

        gestorEnemigosVisual.dibujar(batch, delta);

        batch.end();
    }

    private void volverAlMenu() {

        clienteUDP.volverAlMenu();

        game.setScreen(new MenuPantalla(game));
    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    @Override
    public void dispose() {

        jugadorLocal.dispose();
        jugadorRemoto.dispose();

        gestorEnemigosVisual.dispose();

        renderizadorMapa.dispose();
        mapa.dispose();
    }
}
