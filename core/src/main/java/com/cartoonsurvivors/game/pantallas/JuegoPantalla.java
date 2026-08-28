package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.controles.ControladorEntrada;
import com.cartoonsurvivors.game.entidades.Jugador;
import com.cartoonsurvivors.game.utilidades.Constantes;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_MUNDO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_MUNDO;

public class JuegoPantalla extends ScreenAdapter {
    private  SpriteBatch batch;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);
    private final ControladorEntrada controladorEntrada = new ControladorEntrada();
    private Texture texturaJugador = new Texture("jugador/mordecai.png");
    private Jugador jugador = new Jugador( 100, 100,texturaJugador);

    public JuegoPantalla(SpriteBatch batch) {
        this.batch = batch;
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

        camera.position.set(jugador.getPosicionX(), jugador.getPosicionY(), 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(texturaJugador, jugador.getPosicionX(), jugador.getPosicionY(), 100f, 100f);
        batch.end();
    }
    @Override
    public void show() {
        System.out.println("Entré a la pantalla de juego");
    }

}
