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
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.badlogic.gdx.graphics.Texture;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;

import java.util.*;

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
    private final Map<Integer, EnemigoBasico> enemigos = new HashMap<>();
    private Texture texturaEnemigos;
    private long ultimaSecuenciaEnemigos = -1;

    private final Map<Jugador, Map<Integer, Long>> ultimosAtaques = new IdentityHashMap<>();


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
        texturaEnemigos = new Texture("enemigos/minion.png");
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

        enviarMovimiento(delta);

        recibirEstadoServidor();
        recibirEstadoEnemigos();
        jugadorLocal.getArma().actualizar(delta);
        jugadorRemoto.getArma().actualizar(delta);

        camaraSeguirJugador();

        // Dibujar el mundo.
        viewport.apply();

        renderizadorMapa.setView(camera);
        renderizadorMapa.render();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        jugadorLocal.dibujar(batch, delta);
        jugadorRemoto.dibujar(batch, delta);

// Dibujar todos los enemigos recibidos.
        for (EnemigoBasico enemigo : enemigos.values()) {

            if (enemigo.estaVivo()) {
                enemigo.dibujar(batch, delta);
            }
        }

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

        // ID:X:Y:DX:DY:VIDA:ARMAS
        String[] partes = datos.split(":", -1);

        if (partes.length != 7) {
            return;
        }

        try {

            float x = Float.parseFloat(partes[1]);
            float y = Float.parseFloat(partes[2]);

            float direccionX = Float.parseFloat(partes[3]);
            float direccionY = Float.parseFloat(partes[4]);

            float vida = Float.parseFloat(partes[5]);

            jugador.setPosicion(x, y);
            jugador.setVida(vida);

            jugador.seMueve(direccionX, direccionY);
            jugador.calcularLadoMirada(direccionX);

            // Procesar las armas sincronizadas.
            actualizarArmasJugador(jugador, partes[6]);

        } catch (NumberFormatException e) {

            Gdx.app.error("JuegoPantalla", "Datos de jugador invalidos: " + datos);
        }
    }


    private void actualizarArmasJugador(Jugador jugador, String datosArmas) {

        if (datosArmas.isEmpty()) {
            return;
        }

        Map<Integer, Long> ataquesJugador = ultimosAtaques.computeIfAbsent(jugador, j -> new HashMap<>());

        String[] armasRecibidas = datosArmas.split(";");

        for (String datosArma : armasRecibidas) {

            // SLOT,TIPO,NUMERO_ATAQUE,DIRECCION
            String[] partes = datosArma.split(",", -1);

            if (partes.length != 4) {
                continue;
            }

            try {

                int slot = Integer.parseInt(partes[0]);
                String tipo = partes[1];

                long numeroAtaque = Long.parseLong(partes[2]);
                int direccion = Integer.parseInt(partes[3]);

                if (slot < 0 || numeroAtaque < 0) {
                    continue;
                }

                if (numeroAtaque <= 0) {
                    continue;
                }

                Long ultimoAtaque = ataquesJugador.get(slot);

                // Ya recibimos este ataque o uno mas nuevo.
                if (ultimoAtaque != null && numeroAtaque <= ultimoAtaque) {
                    continue;
                }

                ataquesJugador.put(slot, numeroAtaque);

                // Por ahora solo tenemos Espada.
                if (tipo.equals("ESPADA") && slot == 0) {

                    Arma arma = jugador.getArma();

                    if (arma instanceof Espada espada) {

                        espada.reproducirAtaque(jugador.getHitbox(), direccion);
                    }
                }

            } catch (NumberFormatException e) {

                Gdx.app.error("JuegoPantalla", "Datos de arma invalidos: " + datosArma);
            }
        }
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

        // Identificador del jugador.
        font.draw(batch, "JUGADOR " + idJugadorLocal, 20, ALTO_MUNDO - 20);

        // Vida del jugador local.
        font.draw(batch, "MI VIDA: " + (int) Math.ceil(jugadorLocal.getVida()), 20, ALTO_MUNDO - 60);

        // Vida del otro jugador.
        font.draw(batch, "VIDA COMPAÑERO: " + (int) Math.ceil(jugadorRemoto.getVida()), 20, ALTO_MUNDO - 100);

        if (!estadoRecibido) {

            font.draw(batch, "Esperando posiciones del servidor...", 20, ALTO_MUNDO - 150);
        }

        batch.end();
    }


    private void recibirEstadoEnemigos() {

        String mensaje = clienteUDP.consumirEstadoEnemigos();

        if (mensaje == null) {
            return;
        }

        // ENEMIGOS|secuencia|ID:TIPO:X:Y:VIDA|...

        String[] partes = mensaje.split("\\|", -1);

        if (partes.length < 2) {
            return;
        }

        try {

            long secuencia = Long.parseLong(partes[1]);

            if (secuencia <= ultimaSecuenciaEnemigos) {
                return;
            }

            Set<Integer> idsRecibidos = new HashSet<>();

            // Validar enemigos recibidos.
            for (int i = 2; i < partes.length; i++) {

                String[] datos = partes[i].split(":", -1);

                if (datos.length != 5) {
                    return;
                }

                int id = Integer.parseInt(datos[0]);
                String tipo = datos[1];

                float x = Float.parseFloat(datos[2]);
                float y = Float.parseFloat(datos[3]);
                int vida = Integer.parseInt(datos[4]);

                if (id <= 0 || !tipo.equals("BASICO") || !Float.isFinite(x) || !Float.isFinite(y) || vida < 0 || !idsRecibidos.add(id)) {
                    return;
                }
            }

            // Crear o actualizar enemigos.
            for (int i = 2; i < partes.length; i++) {

                String[] datos = partes[i].split(":", -1);

                int id = Integer.parseInt(datos[0]);
                float x = Float.parseFloat(datos[2]);
                float y = Float.parseFloat(datos[3]);
                int vida = Integer.parseInt(datos[4]);

                EnemigoBasico enemigo = enemigos.get(id);

                if (enemigo == null) {

                    enemigo = new EnemigoBasico(25, 80f, 15, x, y, texturaEnemigos);

                    enemigos.put(id, enemigo);
                }

                enemigo.setPosicion(x, y);
                enemigo.setVida(vida);
            }

            // Eliminar enemigos que ya no están en el servidor.
            List<Integer> idsActuales = new ArrayList<>(enemigos.keySet());

            for (int i = idsActuales.size() - 1; i >= 0; i--) {

                int id = idsActuales.get(i);

                if (!idsRecibidos.contains(id)) {

                    EnemigoBasico eliminado = enemigos.remove(id);

                    if (eliminado != null) {
                        eliminado.dispose();
                    }
                }
            }

            ultimaSecuenciaEnemigos = secuencia;

        } catch (NumberFormatException e) {

            Gdx.app.error("JuegoPantalla", "Estado de enemigos invalido: " + mensaje);
        }
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

        for (EnemigoBasico enemigo : enemigos.values()) {
            enemigo.dispose();
        }

        enemigos.clear();

        if (texturaEnemigos != null) {
            texturaEnemigos.dispose();
        }

        renderizadorMapa.dispose();
        mapa.dispose();
    }
}
