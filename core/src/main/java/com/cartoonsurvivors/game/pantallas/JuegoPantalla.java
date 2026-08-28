package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.Mordecai;
import com.cartoonsurvivors.game.utilidades.Constantes;
import com.cartoonsurvivors.game.CartoonSurvivors;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_MUNDO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_MUNDO;

public class JuegoPantalla extends ScreenAdapter {
    private  SpriteBatch batch;
    private  AudioManager audioManager;

    private final OrthographicCamera camera = new OrthographicCamera();
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);
    private final ControladorEntrada controladorEntrada = new ControladorEntrada();
    private Texture texturaEnemigo = new Texture("enemigos/minion.png");
    private Jugador jugador = new Mordecai();
    private EnemigoBasico enemigo1 = new EnemigoBasico(100, 500, 500, 1);

    public JuegoPantalla(SpriteBatch batch, CartoonSurvivors game) {
        this.batch = batch;
        this.audioManager = game.getAudioManager();
        mapa = new TmxMapLoader().load("mapas/mapa1.tmx");
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa);
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
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();


        float direccionX = controladorEntrada.obtenerDireccionX();
        float direccionY = controladorEntrada.obtenerDireccionY();

        jugador.mover(direccionX * jugador.getVelocidad() * delta, direccionY * jugador.getVelocidad() * delta);
        enemigo1.seguirJugador(jugador);

        camera.position.set(jugador.getPosicionX() + Constantes.Jugador.TAMAÑO_SPRITE / 2, jugador.getPosicionY() + Constantes.Jugador.TAMAÑO_SPRITE / 2, 0);
        camera.update();

        renderizadorMapa.setView(camera);
        renderizadorMapa.render();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(jugador.getTexturaIdle(), jugador.getPosicionX(), jugador.getPosicionY(), 100f, 100f);
        batch.draw(texturaEnemigo, enemigo1.getPosicionX(), enemigo1.getPosicionY()-100, 100f, 100f);
        batch.end();
    }
    @Override
    public void show() {
        jugador.setPosicion(calcularCentroX(), calcularCentroY());
        enemigo1.setPosicion(calcularCentroX() + 200, calcularCentroY() + 200);
        audioManager.reproducirMusicaJuego();
    }

}
