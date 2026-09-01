package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoSpawner;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.Mordecai;
import com.cartoonsurvivors.game.utilidades.Constantes;
import com.cartoonsurvivors.game.CartoonSurvivors;

import java.util.ArrayList;

import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.*;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;

public class JuegoPantalla extends ScreenAdapter {
    private  SpriteBatch batch;
    private  AudioManager audioManager;

    private final OrthographicCamera camera = new OrthographicCamera();
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);
    private final ControladorEntrada controladorEntrada = new ControladorEntrada();
    private Jugador jugador = new Mordecai();
    private EnemigoSpawner enemigoSpawner;
    private float tiempoSpawn;
    private Array<EnemigoBasico> enemigos = new Array<>();


    public JuegoPantalla( CartoonSurvivors game) {
        this.batch = game.getBatch();
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

        tiempoSpawn += delta;
        float direccionX = controladorEntrada.obtenerDireccionX();
        float direccionY = controladorEntrada.obtenerDireccionY();
        boolean mirandoDerecha = false;


        jugador.mover(direccionX * jugador.getVelocidad() * delta, direccionY * jugador.getVelocidad() * delta);
        boolean seEstaMoviendo = direccionX != 0 || direccionY != 0;

        mirandoDerecha = calcularLadoMirada(direccionX, mirandoDerecha);

        camaraSeguirJugador();
        aparicionEnemigos();

        actualizarEnemigos(delta);
        renderizadorMapa.setView(camera);
        renderizadorMapa.render();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        dibujarEnemigos(delta);
        dibujarJugador(delta, seEstaMoviendo, mirandoDerecha);

        batch.end();
    }

    private boolean calcularLadoMirada(float direccionX, boolean mirandoDerecha) {
        if (direccionX < 0) {
            mirandoDerecha = true;
        }

        if (direccionX > 0) {
            mirandoDerecha = false;
        }
        return mirandoDerecha;
    }

    private void camaraSeguirJugador() {
        camera.position.set(jugador.getPosicionX() + Constantes.Jugador.TAMAÑO_SPRITE / 2, jugador.getPosicionY() + Constantes.Jugador.TAMAÑO_SPRITE / 2, 0);
        camera.update();
    }

    private void aparicionEnemigos() {
        if (tiempoSpawn >= TIEMPO_APARICION) {
            tiempoSpawn = 0;
            float posicionX;
            float posicionY;
            int lado = MathUtils.random(3);

            switch (lado) {

                case 0: // Arriba
                    posicionX = jugador.getPosicionX() + MathUtils.random(-ANCHO_MUNDO / 2f, ANCHO_MUNDO / 2f);
                    posicionY = jugador.getPosicionY() + ALTO_MUNDO / 2f + MARGEN_APARICION;
                    break;
                case 1: // Abajo
                    posicionX = jugador.getPosicionX() + MathUtils.random(-ANCHO_MUNDO / 2f, ANCHO_MUNDO / 2f);
                    posicionY = jugador.getPosicionY() - ALTO_MUNDO / 2f - MARGEN_APARICION;
                    break;
                case 2: // Izquierda
                    posicionX = jugador.getPosicionX() - ANCHO_MUNDO / 2f - MARGEN_APARICION;
                    posicionY = jugador.getPosicionY() + MathUtils.random(-ALTO_MUNDO / 2f, ALTO_MUNDO / 2f);
                    break;
                default: // Derecha
                    posicionX = jugador.getPosicionX() + ANCHO_MUNDO / 2f + MARGEN_APARICION;
                    posicionY = jugador.getPosicionY() + MathUtils.random(-ALTO_MUNDO / 2f, ALTO_MUNDO / 2f);
                    break;
            }
            EnemigoBasico enemigo = enemigoSpawner.spawn(posicionX, posicionY );
            enemigos.add(enemigo);
        }
    }

    private void actualizarEnemigos(float delta) {
        for (EnemigoBasico enemigo : enemigos) {
            enemigo.seguirJugador(jugador.getPosicionX(), jugador.getPosicionY(), delta
            );
        }
    }

    private void dibujarJugador(float delta, boolean seEstaMoviendo, boolean mirandoDerecha) {
        if (seEstaMoviendo) {
            TextureRegion frame = jugador.getFrameCaminar(delta);

            if (mirandoDerecha) {
                if (frame.isFlipX()) {
                    frame.flip(true, false);
                }
            } else {
                if (!frame.isFlipX()) {
                    frame.flip(true, false);
                }
            }
            batch.draw(frame, jugador.getPosicionX(), jugador.getPosicionY(), 100f, 100f);
        } else {
            batch.draw(jugador.getTexturaIdle(), jugador.getPosicionX(), jugador.getPosicionY(), 100f, 100f);
        }
    }
    private void dibujarEnemigos(float delta) {
        for (EnemigoBasico enemigo : enemigos) {
            if(enemigo.isMirandoIzquierda()) {
                if (!enemigo.getFrameCaminar(delta).isFlipX()) {
                    enemigo.getFrameCaminar(delta).flip(true, false);
                }
            } else {
                if (enemigo.getFrameCaminar(delta).isFlipX()) {
                    enemigo.getFrameCaminar(delta).flip(true, false);
                }
            }
            batch.draw(enemigo.getFrameCaminar(delta), enemigo.getPosicionX(), enemigo.getPosicionY(), ANCHO_ENEMIGO, ALTO_ENEMIGO);
        }
    }

    @Override
    public void show() {
        jugador.setPosicion(calcularCentroX(), calcularCentroY());
        audioManager.reproducirMusicaJuego();
        enemigoSpawner = new EnemigoSpawner( 100, 50f, 10, new Texture("enemigos/minion.png"));

    }




}
