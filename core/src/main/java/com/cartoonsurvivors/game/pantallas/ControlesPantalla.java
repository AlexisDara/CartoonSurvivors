package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.utilidades.Constantes;

import java.awt.*;

public class ControlesPantalla extends ScreenAdapter {

    private final CartoonSurvivors game;
    private boolean pasarPantalla = false;
    private final ControladorEntrada controladorEntrada = new ControladorEntrada();
    private final AudioManager audioManager;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    public ControlesPantalla(CartoonSurvivors game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(Constantes.Mundo.ANCHO_INTERFAZ, Constantes.Mundo.ALTO_INTERFAZ, camera);
        this.audioManager = game.getAudioManager();
    }
    @Override
    public void show() {


        font.getData().setScale(0.5f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0.2f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (controladorEntrada.pasarPantalla()) {
            game.setScreen(new JuegoPantalla(game));
            audioManager.detenerMusicaMenu();
        }

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        font.draw(batch, "Controles del Juego", 100, 400);
        font.draw(batch, "Arriba: W", 100, 350);
        font.draw(batch, "Abajo: S", 100, 300);
        font.draw(batch, "Izquierda: A", 100, 250);
        font.draw(batch, "Derecha: D", 100, 200);
        font.draw(batch, "Presiona Espacio para continuar", 100, 100);
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);    }




}
