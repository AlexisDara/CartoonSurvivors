package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;
import com.cartoonsurvivors.game.CartoonSurvivors;


public class ControlesPantalla extends ScreenAdapter {
    private final CartoonSurvivors game;
    private final Batch batch;
    private final BitmapFont font;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);;

    public ControlesPantalla(CartoonSurvivors game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();

    }

    @Override
    public void resize(int width, int height) {
       viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.RED);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.draw(batch, "hola juan carlos como andas chupapija", 10, 50);

        batch.end();

    }


}
