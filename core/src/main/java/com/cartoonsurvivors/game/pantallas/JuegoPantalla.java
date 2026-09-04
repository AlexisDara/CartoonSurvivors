package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoSpawner;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.Mordecai;
import com.cartoonsurvivors.game.utilidades.Constantes;
import com.cartoonsurvivors.game.CartoonSurvivors;
import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.*;
import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;
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
    private EnemigoSpawner enemigoSpawner;
    private float tiempoSpawn;
    private float tiempoJuego;
    private Array<EnemigoBasico> enemigos = new Array<>();
    private final Array<EnemigoBasico> enemigosGolpeados = new Array<>();
    private int enemigosMatados = 0;

    private ShapeRenderer shapeRenderer;

    private PantallaPausa pantallaPausa;

    public JuegoPantalla( CartoonSurvivors game) {
        this.game = game;
        this.font = game.getFont();
        this.batch = game.getBatch();
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


                tiempoSpawn += delta;
                tiempoJuego += delta;
                float direccionX = controladorEntrada.obtenerDireccionX();
                float direccionY = controladorEntrada.obtenerDireccionY();
                estadoJuego = controladorEntrada.pausarPantalla(estadoJuego, pantallaPausa, audioManager);

                actualizarJugador(delta, direccionX, direccionY);
                camaraSeguirJugador();
                tiempoSpawn = enemigoSpawner.aparicionEnemigos(jugador, tiempoSpawn, enemigos);
                actualizarEnemigos(delta);
                renderizadorMapa.setView(camera);
                renderizadorMapa.render();
                chequeoColisiones(delta);
                chequeoAtaque();

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

        for (EnemigoBasico enemigo : enemigos) {
            enemigo.dibujar(batch, delta);
        }
        jugador.dibujar(batch, delta);

        batch.end();

        HUDViewport.apply();
        batch.setProjectionMatrix(HUDViewport.getCamera().combined);
        batch.begin();
        font.draw(batch, "Vida: " + String.format("%.0f", jugador.getVida()), 10, ALTO_MUNDO - 10);
        font.draw(batch, "Tiempo: " + String.format("%.0f", tiempoJuego), (ANCHO_MUNDO /2) - 90f, ALTO_MUNDO - 10);
        font.draw(batch, "Enemigos: " + enemigosMatados, 10, ALTO_MUNDO - 35
        );
        batch.end();
    }

    private void chequeoAtaque() {
        if (!jugador.getArma().estaAtacando()) {
            return;
        }
        Rectangle areaAtaque = jugador.getArma().getAreaAtaque();
        for (EnemigoBasico enemigo : enemigos) {
            if (!enemigo.estaVivo()) {
                continue;
            }
            if (areaAtaque.overlaps(enemigo.getHitbox())) {
                enemigo.recibirDanio(jugador.getArma().getDanio());
            }
        }
    }

    private void mostrarHitbox() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        Rectangle r = jugador.getHitbox();
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        for (EnemigoBasico enemigo : enemigos) {
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

        dibujarPantallaVictoriaDerrota(Color.RED, "DERROTA");
        if(controladorEntrada.reiniciarJuego(audioManager)) {
            reiniciarJuego(audioManager);
        }
        if (controladorEntrada.volverMenu(audioManager)) {
            game.setScreen(new MenuPantalla(game));
        }
    }



    private void mostrarPantallaVictoria() {

        dibujarPantallaVictoriaDerrota(Color.GREEN, "VICTORIA");
        if(controladorEntrada.reiniciarJuego(audioManager)) {
            reiniciarJuego(audioManager);
        }
        if (controladorEntrada.volverMenu(audioManager)) {
            game.setScreen(new MenuPantalla(game));
        }
    }

    private void dibujarPantallaVictoriaDerrota(Color color, String texto) {
        GlyphLayout layout = new GlyphLayout();
        batch.setProjectionMatrix(HUDViewport.getCamera().combined);
        batch.begin();
        font.setColor(color);
        String titulo = texto;
        layout.setText(font, titulo);
        font.draw(batch, titulo, ANCHO_MUNDO / 2f - layout.width / 2f, 500);
        font.setColor(1, 1, 1, 1);
        String enemigos = "Enemigos eliminados: " + enemigosMatados;
        layout.setText(font, enemigos);
        font.draw(batch, enemigos, ANCHO_MUNDO / 2f - layout.width / 2f, 430);
        String tiempo = "Tiempo sobrevivido: " + String.format("%.0f", tiempoJuego) + " segundos";
        layout.setText(font, tiempo);
        font.draw(batch, tiempo, ANCHO_MUNDO / 2f - layout.width / 2f, 370);
        String reiniciar = "Toca R para reiniciar";
        layout.setText(font, reiniciar);
        font.draw(batch, reiniciar, ANCHO_MUNDO / 2f - layout.width / 2f, 270);
        String menu = "Toca M para volver al menú";
        layout.setText(font, menu);
        font.draw(batch, menu, ANCHO_MUNDO / 2f - layout.width / 2f, 210);
        batch.end();
    }

    private void chequeoColisiones(float delta) {
        int numeroHits = 0;
        for (EnemigoBasico enemigo : enemigos) {
            if (jugador.colisionaCon(enemigo)) {
                numeroHits++;
            }
        }
        if(numeroHits > 0) {
            jugador.recibirDanio(numeroHits * DANIO_ENEMIGO * delta);
        }
    }

    private void camaraSeguirJugador() {
        camera.position.set(jugador.getPosicionX() + Constantes.Jugador.TAMAÑO_SPRITE / 2, jugador.getPosicionY() + Constantes.Jugador.TAMAÑO_SPRITE / 2, 0);
        camera.update();
    }

    private void actualizarEnemigos(float delta) {

        for (int i = enemigos.size - 1; i >= 0; i--) {

            EnemigoBasico enemigo = enemigos.get(i);

            if (!enemigo.estaVivo()) {
                enemigos.removeIndex(i);
                enemigosMatados++;
                continue;
            }

            enemigo.seguirJugador(jugador.getPosicionX(), jugador.getPosicionY(), delta);
        }
    }


    private void reiniciarJuego(AudioManager audioManager) {
        jugador.setPosicion(calcularCentroX(), calcularCentroY());
        jugador.setVida(VIDA_INICIAL);
        estadoJuego=EstadoJuego.JUGANDO;
        enemigos.clear();
        audioManager.reproducirMusicaJuego();
        tiempoSpawn = 0;
        tiempoJuego = 0;
        enemigosMatados = 0;
    }

    @Override
    public void show() {
        jugador.setPosicion(calcularCentroX(), calcularCentroY());
        estadoJuego=EstadoJuego.JUGANDO;
        jugador.setVida(VIDA_INICIAL);
        audioManager.reproducirMusicaJuego();
        enemigoSpawner = new EnemigoSpawner( VIDA_ENEMIGO, VELOCIDAD_ENEMIGO, DANIO_ENEMIGO, new Texture("enemigos/minion.png"));
        estadoJuego = EstadoJuego.JUGANDO;
        tiempoSpawn = 0;
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (pantallaPausa != null) pantallaPausa.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

}
