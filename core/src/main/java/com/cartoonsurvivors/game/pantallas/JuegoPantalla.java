package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;

public class JuegoPantalla extends ScreenAdapter {

    private static final float INTERVALO_MOVIMIENTO = 0.05f;

    private final CartoonSurvivors game;
    private final ClienteUDP clienteUDP;

    private final SpriteBatch batch;
    private final BitmapFont font;

    private final OrthographicCamera camera = new OrthographicCamera();

    private final OrthographicCamera hudCamera = new OrthographicCamera();

    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);

    private final ExtendViewport hudViewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, hudCamera);

    private final ControladorEntrada controladorEntrada = new ControladorEntrada();

    private final TiledMap mapa;
    private final OrthogonalTiledMapRenderer renderizadorMapa;

    private final Jugador jugadorLocal;
    private final Jugador jugadorRemoto;

    private final int idJugadorLocal;

    private float tiempoEnvio = INTERVALO_MOVIMIENTO;

    private long ultimaSecuencia = -1;

    private boolean estadoRecibido = false;

    public JuegoPantalla(CartoonSurvivors game, Jugador jugador) {

        this.game = game;
        this.clienteUDP = game.getClienteUDP();

        this.batch = game.getBatch();
        this.font = game.getFont();

        this.jugadorLocal = jugador;

        // Por ahora el único personaje disponible es Mordecai.
        this.jugadorRemoto = TipoPersonaje.MORDECAI.crearPersonaje();

        this.idJugadorLocal = clienteUDP.getIdJugador();

        mapa = new TmxMapLoader().load("mapas/mapa1.tmx");

        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa);
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(null);

        // Posiciones iniciales del servidor.
        jugadorLocal.setPosicion(idJugadorLocal == 1 ? 6340 : 6460, 6400);

        jugadorRemoto.setPosicion(idJugadorLocal == 1 ? 6460 : 6340, 6400);

        camaraSeguirJugador();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // La tecla ESC permite abandonar la sesión.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            volverAlMenu();
            return;
        }

        // Si se pierde la conexión no continuamos jugando.
        if (clienteUDP.estaServidorDesconectado() || clienteUDP.estaSesionFinalizada()) {

            mostrarSesionFinalizada();
            return;
        }

        // No continuar jugando si terminó la partida.
        if (!clienteUDP.estaPartidaIniciada()) {

            mostrarSesionFinalizada();
            return;
        }

        // Enviar las direcciones de nuestro teclado.
        enviarMovimiento(delta);

        // Obtener las posiciones calculadas por el servidor.
        recibirEstadoServidor();

        // La cámara sigue solamente al jugador local.
        camaraSeguirJugador();

        // Dibujar el mundo.
        viewport.apply();

        renderizadorMapa.setView(camera);
        renderizadorMapa.render();

        // Dibujar ambos jugadores.
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        jugadorLocal.dibujar(batch, delta);
        jugadorRemoto.dibujar(batch, delta);

        batch.end();

        // Dibujar información sobre la pantalla.
        dibujarInformacion();
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

    private void recibirEstadoServidor() {

        String mensaje = clienteUDP.consumirUltimoEstado();

        if (mensaje == null) {
            return;
        }

        // ESTADO|secuencia|datosJ1|datosJ2
        String[] partes = mensaje.split("\\|");

        if (partes.length != 4) {
            return;
        }

        try {

            long secuencia = Long.parseLong(partes[1]);

            // UDP puede entregar paquetes desordenados.
            if (secuencia <= ultimaSecuencia) {
                return;
            }

            String datosJugador1 = partes[2];
            String datosJugador2 = partes[3];

            // Elegir qué datos corresponden a cada jugador.
            if (idJugadorLocal == 1) {

                actualizarJugador(jugadorLocal, datosJugador1);

                actualizarJugador(jugadorRemoto, datosJugador2);

            } else {

                actualizarJugador(jugadorLocal, datosJugador2);

                actualizarJugador(jugadorRemoto, datosJugador1);
            }

            ultimaSecuencia = secuencia;
            estadoRecibido = true;

        } catch (NumberFormatException e) {

            Gdx.app.error("JuegoPantalla", "Estado numerico invalido: " + mensaje);
        }
    }

    private void actualizarJugador(Jugador jugador, String datos) {

        // Formato: ID:X:Y:DIRECCION_X:DIRECCION_Y
        String[] partes = datos.split(":");

        if (partes.length != 5) {
            return;
        }

        float x = Float.parseFloat(partes[1]);
        float y = Float.parseFloat(partes[2]);

        float direccionX = Float.parseFloat(partes[3]);
        float direccionY = Float.parseFloat(partes[4]);

        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(direccionX) || !Float.isFinite(direccionY)) {
            return;
        }

        // La posicion viene del servidor.
        jugador.setPosicion(x, y);

        // Actualizar animacion de movimiento.
        jugador.seMueve(direccionX, direccionY);

        // Actualizar hacia donde mira.
        jugador.calcularLadoMirada(direccionX);
    }

    private void camaraSeguirJugador() {

        camera.position.set(jugadorLocal.getPosicionX() + 20, jugadorLocal.getPosicionY() + 30, 0);

        camera.update();
    }

    private void dibujarInformacion() {

        hudViewport.apply();

        batch.setProjectionMatrix(hudCamera.combined);

        batch.begin();

        font.setColor(Color.WHITE);

        font.draw(batch, "JUGADOR " + idJugadorLocal, 20, ALTO_MUNDO - 20);

        if (!estadoRecibido) {

            font.draw(batch, "Esperando posiciones del servidor...", 20, ALTO_MUNDO - 75);
        }

        batch.end();
    }

    private void mostrarSesionFinalizada() {

        hudViewport.apply();

        batch.setProjectionMatrix(hudCamera.combined);

        batch.begin();

        font.setColor(Color.RED);

        String mensaje;

        if (clienteUDP.estaServidorDesconectado()) {
            mensaje = "SERVIDOR DESCONECTADO";
        } else {
            mensaje = "SESION FINALIZADA";
        }

        font.draw(batch, mensaje, 350, 400);

        font.setColor(Color.WHITE);

        font.draw(batch, "Presiona ESC para volver al menu", 310, 320);

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

        renderizadorMapa.dispose();
        mapa.dispose();
    }
}
