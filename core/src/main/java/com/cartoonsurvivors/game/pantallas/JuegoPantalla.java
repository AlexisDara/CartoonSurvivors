package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.cartoonsurvivors.game.utilidades.TipoMapa;

import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;
import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;

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
    private final Texture tumba;
    private final Jugador jugadorLocal;
    private Jugador jugadorRemoto;
    private TipoPersonaje tipoRemoto;
    private final SincronizadorJugadores sincronizadorJugadores;
    private final GestorEnemigos gestorEnemigosVisual;
    private final HUDJuego hudJuego;
    private final MenuMejoras menuMejoras;
    private final PantallaPausa pantallaPausa;
    private final ControlPausa controlPausa = new ControlPausa();
    private final SeleccionMejora seleccionMejora = new SeleccionMejora();
    private final int idJugadorLocal;
    private float tiempoEnvio = INTERVALO_MOVIMIENTO;
    private boolean salidaSolicitada;

    public JuegoPantalla(CartoonSurvivors game, Jugador jugador) {

        this.game = game;
        this.clienteUDP = game.getClienteUDP();
        this.batch = game.getBatch();

        jugadorLocal = jugador;

        tipoRemoto = clienteUDP.getPersonajeRemoto();
        jugadorRemoto = tipoRemoto.crearPersonaje();

        idJugadorLocal = clienteUDP.getIdJugador();

        TipoMapa tipoMapa = clienteUDP.getMapaPartida();
        mapa = new TmxMapLoader().load(tipoMapa.getRuta());
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa);
        tumba = new Texture(Gdx.files.internal("jugador/tumba.png"));

        sincronizadorJugadores = new SincronizadorJugadores(jugadorLocal, jugadorRemoto, idJugadorLocal);

        gestorEnemigosVisual = new GestorEnemigos(tipoMapa);

        hudJuego = new HUDJuego(batch, game.getFont(), hudViewport);
        menuMejoras = new MenuMejoras(hudJuego, batch, game.getFont(), hudViewport);
        pantallaPausa = new PantallaPausa(game, hudViewport,
            () -> controlPausa.solicitar(sincronizadorJugadores.getEstadisticasPartida(), true),
            () -> salidaSolicitada = true);
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(null);
        game.getAudioManager().detenerMusicaMenu();
        game.getAudioManager().reproducirMusicaJuego();

        jugadorLocal.setPosicion(idJugadorLocal == 1 ? 6340 : 6460, 6400);

        jugadorRemoto.setPosicion(idJugadorLocal == 1 ? 6460 : 6340, 6400);

        camaraSeguirJugador();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (salidaSolicitada) {
            volverAlMenu();
            return;
        }

        boolean escape = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        sincronizadorJugadores.procesarEstadoHud(clienteUDP.consumirEstadoHud());
        EstadisticasPartida estadisticas = sincronizadorJugadores.getEstadisticasPartida();
        if (estadisticas != null && estadisticas.resultado().esFinal()) {
            if (escape) {
                volverAlMenu();
                return;
            }
            activarEntradaPausa(false);
            hudJuego.dibujarResultado(estadisticas.resultado(), estadisticas.kills());
            return;
        }

        if (clienteUDP.estaServidorDesconectado() || clienteUDP.estaSesionFinalizada() || !clienteUDP.estaPartidaIniciada()) {
            if (escape) {
                volverAlMenu();
                return;
            }
            activarEntradaPausa(false);
            hudJuego.dibujarSesionFinalizada(clienteUDP.estaServidorDesconectado());

            return;
        }

        actualizarEstados();
        var pausa = estadisticas == null ? null : estadisticas.pausa();
        boolean pausaManual = pausa != null && pausa.activa();
        boolean enMejora = estadisticas != null && estadisticas.rondaMejora() != null;
        if (escape && !pausaManual) {
            controlPausa.solicitar(estadisticas, false);
        }
        controlPausa.actualizar(delta, estadisticas, idJugadorLocal, clienteUDP);
        activarEntradaPausa(pausaManual);
        if (!pausaManual) {
            seleccionMejora.actualizar(delta, estadisticas, clienteUDP, menuMejoras);
        }

        boolean enPausa = pausaManual || enMejora;
        if (!enPausa) {
            enviarMovimiento(delta);
            jugadorLocal.getArmas().forEach(arma -> arma.actualizar(delta));
            jugadorRemoto.getArmas().forEach(arma -> arma.actualizar(delta));
        }

        camaraSeguirJugador();
        dibujarMundo(enPausa ? 0f : delta);

        hudJuego.dibujarMultijugador(jugadorLocal, jugadorRemoto, sincronizadorJugadores.hayEstadoRecibido(),
            estadisticas, sincronizadorJugadores.getPasivasLocales());
        if (!enPausa && sincronizadorJugadores.hayEstadoRecibido()) {
            indicarCompaneroFueraDePantalla();
        }
        if (pausaManual) {
            pantallaPausa.render(delta, pausa, idJugadorLocal, controlPausa.enviandoConfirmacion());
        } else if (enMejora) {
            menuMejoras.dibujar(jugadorLocal, jugadorRemoto, estadisticas, seleccionMejora.seleccion(),
                seleccionMejora.esperandoConfirmacion(estadisticas));
        }
    }

    private void activarEntradaPausa(boolean activa) {
        var stage = pantallaPausa.getStage();
        if (activa && Gdx.input.getInputProcessor() != stage) {
            Gdx.input.setInputProcessor(stage);
        } else if (!activa && Gdx.input.getInputProcessor() == stage) {
            stage.cancelTouchFocus();
            Gdx.input.setInputProcessor(null);
        }
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
        TipoPersonaje tipoRecibido = clienteUDP.getPersonajeRemoto();
        if (tipoRecibido != tipoRemoto) {
            Jugador anterior = jugadorRemoto;
            jugadorRemoto = tipoRecibido.crearPersonaje();
            jugadorRemoto.setPosicion(anterior.getPosicionX(), anterior.getPosicionY());
            jugadorRemoto.setVida(anterior.getVida());
            jugadorRemoto.setVidaMaxima(anterior.getVidaMaxima());
            jugadorRemoto.setSegundosParaReaparecer(anterior.getSegundosParaReaparecer());
            sincronizadorJugadores.cambiarJugadorRemoto(jugadorRemoto);
            anterior.dispose();
            tipoRemoto = tipoRecibido;
        }
        sincronizadorJugadores.procesarEstado(clienteUDP.consumirUltimoEstado());

        gestorEnemigosVisual.procesarEstado(clienteUDP.consumirEstadoEnemigos());
    }

    private void camaraSeguirJugador() {

        camera.position.set(jugadorLocal.getPosicionX() + 20, jugadorLocal.getPosicionY() + 30, 0);

        camera.update();
    }

    private void indicarCompaneroFueraDePantalla() {
        float dx = jugadorRemoto.getHitbox().x + jugadorRemoto.getHitbox().width / 2f - camera.position.x;
        float dy = jugadorRemoto.getHitbox().y + jugadorRemoto.getHitbox().height / 2f - camera.position.y;
        float margen = TAMAÑO_REAL / 2f;
        if (Math.abs(dx) > viewport.getWorldWidth() * camera.zoom / 2f + margen
            || Math.abs(dy) > viewport.getWorldHeight() * camera.zoom / 2f + margen) {
            hudJuego.dibujarFlechaCompanero(dx, dy);
        }
    }

    private void dibujarMundo(float delta) {

        viewport.apply();

        renderizadorMapa.setView(camera);
        renderizadorMapa.render();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        gestorEnemigosVisual.dibujarFuego(batch);
        dibujarJugador(jugadorLocal, delta);
        dibujarJugador(jugadorRemoto, delta);

        gestorEnemigosVisual.dibujar(batch, delta);
        gestorEnemigosVisual.dibujarBalas(batch);

        if (sincronizadorJugadores.hayEstadoRecibido()) {
            hudJuego.dibujarBarraJugador(jugadorLocal);
            hudJuego.dibujarBarraJugador(jugadorRemoto);
        }

        batch.end();
    }

    private void dibujarJugador(Jugador jugador, float delta) {
        if (jugador.getVida() > 0f) {
            jugador.dibujar(batch, delta);
            return;
        }

        float escala = TAMAÑO_REAL / Math.max(tumba.getWidth(), tumba.getHeight());
        float ancho = tumba.getWidth() * escala;
        float alto = tumba.getHeight() * escala;
        batch.draw(tumba, jugador.getHitbox().getX() + (jugador.getHitbox().getWidth() - ancho) / 2f, jugador.getHitbox().getY() + (jugador.getHitbox().getHeight() - alto) / 2f, ancho, alto);
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
    public void hide() {
        activarEntradaPausa(false);
        game.getAudioManager().detenerMusicaJuego();
    }

    @Override
    public void dispose() {

        jugadorLocal.dispose();
        jugadorRemoto.dispose();

        gestorEnemigosVisual.dispose();
        hudJuego.dispose();
        menuMejoras.dispose();
        pantallaPausa.dispose();
        tumba.dispose();

        renderizadorMapa.dispose();
        mapa.dispose();
    }
}
