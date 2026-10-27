package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoSpawner;
import com.cartoonsurvivors.game.entidades.enemigos.GestorEnemigos;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.Mordecai;
import com.cartoonsurvivors.game.sistemas.SistemaColisiones;
import com.cartoonsurvivors.game.utilidades.Constantes;
import com.cartoonsurvivors.game.CartoonSurvivors;
import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.*;
import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.VIDA_INICIAL;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;
import com.cartoonsurvivors.game.utilidades.EstadoJuego;
import java.awt.*;

public class JuegoPantalla extends ScreenAdapter {
    private CartoonSurvivors game;
    private  SpriteBatch batch;
    private  AudioManager audioManager;
    private  EstadoJuego estadoJuego = EstadoJuego.JUGANDO;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private TiledMap mapa;
    private final BitmapFont font;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);
    private final ExtendViewport HUDViewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, hudCamera);
    private final ControladorEntrada controladorEntrada = new ControladorEntrada();
    private Jugador jugador = new Mordecai();
    private float tiempoJuego;
    private final SistemaColisiones sistemaColisiones = new SistemaColisiones();
    private ShapeRenderer shapeRenderer;
    private PantallaPausa pantallaPausa;
    private final HUDJuego hudJuego;
    private GestorEnemigos gestorEnemigos;
    private Texture texturaEnemigos;



    public JuegoPantalla( CartoonSurvivors game) {
        this.game = game;
        this.font = game.getFont();
        this.batch = game.getBatch();
        this.hudJuego = new HUDJuego(batch, font, HUDViewport);
        this.audioManager = game.getAudioManager();
        mapa = new TmxMapLoader().load("mapas/mapa1.tmx");
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa);
        shapeRenderer = new ShapeRenderer();
        this.pantallaPausa = new PantallaPausa(game);
        configurarCallbacksPausa();
    }

    private void configurarCallbacksPausa() {
        pantallaPausa.setOnContinuar(() -> {
            estadoJuego = EstadoJuego.JUGANDO;
            Gdx.input.setInputProcessor(null); // Devuelve el control al juego
            audioManager.reanudarMusicaJuego();
        });

        pantallaPausa.setOnReiniciar(() -> {
            reiniciarJuego(audioManager);
            estadoJuego = EstadoJuego.JUGANDO;
            Gdx.input.setInputProcessor(null);
            audioManager.reanudarMusicaJuego();
        });

        pantallaPausa.setOnMenuPrincipal(() -> {
            game.setScreen(new MenuPantalla(game));
        });
    }

    private int calcularCentroX() {
        int anchoMapa = (mapa.getProperties().get("width", Integer.class)) * (mapa.getProperties().get("tilewidth", Integer.class));
        return anchoMapa / 2;
    }
    private int calcularCentroY() {
        int altoMapa = (mapa.getProperties().get("height", Integer.class)) * (mapa.getProperties().get("tileheight", Integer.class));
        return altoMapa / 2;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        HUDViewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        if (jugador.estaVivo()) {

            if (estadoJuego == EstadoJuego.JUGANDO) {

                tiempoJuego += delta;

                float direccionX = controladorEntrada.obtenerDireccionX();
                float direccionY = controladorEntrada.obtenerDireccionY();
                estadoJuego = controladorEntrada.pausarPantalla(estadoJuego, pantallaPausa, audioManager);

                actualizarJugador(delta, direccionX, direccionY);
                camaraSeguirJugador();
                gestorEnemigos.actualizar(delta, jugador);
                renderizadorMapa.setView(camera);
                renderizadorMapa.render();
                sistemaColisiones.comprobarColisionesJugador(jugador, gestorEnemigos.getEnemigos(), delta);
                sistemaColisiones.comprobarAtaques(jugador, gestorEnemigos.getEnemigos());

            batch.setProjectionMatrix(camera.combined);

            dibujar(delta);
            }
            //mostrarHitbox();

            if (estadoJuego == EstadoJuego.PAUSADO) {
                pantallaPausa.render(delta);
            }
            if(estadoJuego == EstadoJuego.VICTORIA ) {
                audioManager.detenerMusicaJuego();
                mostrarPantallaVictoria();
            }
            if(tiempoJuego>=200) {
                estadoJuego = EstadoJuego.VICTORIA;

            }
        } else if(!jugador.estaVivo()) {
            mostrarPantallaMuerte();
        }


    }

    private void actualizarJugador(float delta, float direccionX, float direccionY) {
        jugador.mover(direccionX, direccionY, delta);
        jugador.seMueve(direccionX, direccionY);
        jugador.calcularLadoMirada(direccionX);
        jugador.actualizarArma(delta);
    }

    private void dibujar(float delta) {
        batch.begin();

        for (EnemigoBasico enemigo : gestorEnemigos.getEnemigos()) {
            enemigo.dibujar(batch, delta);
        }
        jugador.dibujar(batch, delta);

        batch.end();

        hudJuego.dibujarHUD(jugador, tiempoJuego, gestorEnemigos.getEnemigosMatados());
    }

    private void mostrarHitbox() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        Rectangle r = jugador.getHitbox();
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        for (EnemigoBasico enemigo : gestorEnemigos.getEnemigos()) {
            Rectangle re = enemigo.getHitbox();
            shapeRenderer.rect(re.x, re.y, re.width, re.height);
        }
        if (jugador.getArma().estaAtacando()) {
            shapeRenderer.setColor(Color.GREEN);

            Rectangle ataque = jugador.getArma().getAreaAtaque();

            shapeRenderer.rect(
                ataque.x,
                ataque.y,
                ataque.width,
                ataque.height
            );
        }
        shapeRenderer.end();
    }

    private void mostrarPantallaMuerte() {

        hudJuego.dibujarPantallaVictoriaDerrota(Color.RED, "DERROTA", gestorEnemigos.getEnemigosMatados(), tiempoJuego);
        if(controladorEntrada.reiniciarJuego(audioManager)) {
            reiniciarJuego(audioManager);
        }
        if (controladorEntrada.volverMenu(audioManager)) {
            game.setScreen(new MenuPantalla(game));
        }
    }



    private void mostrarPantallaVictoria() {

        hudJuego.dibujarPantallaVictoriaDerrota(Color.GREEN, "VICTORIA", gestorEnemigos.getEnemigosMatados(), tiempoJuego);
        if(controladorEntrada.reiniciarJuego(audioManager)) {
            reiniciarJuego(audioManager);
        }
        if (controladorEntrada.volverMenu(audioManager)) {
            game.setScreen(new MenuPantalla(game));
        }
    }

    private void camaraSeguirJugador() {
        camera.position.set(jugador.getPosicionX() + Constantes.Jugador.TAMAÑO_SPRITE / 2, jugador.getPosicionY() + Constantes.Jugador.TAMAÑO_SPRITE / 2, 0);
        camera.update();
    }


    private void reiniciarJuego(AudioManager audioManager) {
        jugador.setPosicion(calcularCentroX(), calcularCentroY());
        jugador.setVida(VIDA_INICIAL);
        estadoJuego=EstadoJuego.JUGANDO;
        gestorEnemigos.reiniciar();
        audioManager.reproducirMusicaJuego();
        tiempoJuego = 0;
    }

    @Override
    public void show() {
        jugador.setPosicion(calcularCentroX(), calcularCentroY());
        estadoJuego=EstadoJuego.JUGANDO;
        jugador.setVida(VIDA_INICIAL);
        audioManager.reproducirMusicaJuego();

        estadoJuego = EstadoJuego.JUGANDO;

        Gdx.input.setInputProcessor(null);
        texturaEnemigos = new Texture("enemigos/minion.png");
        EnemigoSpawner enemigoSpawner = new EnemigoSpawner(VIDA_ENEMIGO, VELOCIDAD_ENEMIGO, DANIO_ENEMIGO, texturaEnemigos);
        gestorEnemigos = new GestorEnemigos(enemigoSpawner);
    }

    @Override
    public void dispose() {

        if (pantallaPausa != null) pantallaPausa.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (renderizadorMapa != null) renderizadorMapa.dispose();
        if (mapa != null) mapa.dispose();
        if (jugador != null) jugador.dispose();
        if (texturaEnemigos != null) texturaEnemigos.dispose();
    }

}
