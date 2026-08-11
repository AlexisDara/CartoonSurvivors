package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.*;
import com.cartoonsurvivors.game.CartoonSurvivors;


public class MenuPrincipal  extends ScreenAdapter {
    private final CartoonSurvivors game;
    private final Batch batch;
    private final BitmapFont font;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport = new ExtendViewport(ANCHO_MUNDO, ALTO_MUNDO, camera);
    private Texture fondo;

    public MenuPrincipal(CartoonSurvivors game) {
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

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();


        batch.draw(fondo, 0, 0, ANCHO_MUNDO, ALTO_MUNDO);
        font.draw(batch, "hola juan carlos como andas chupapija", 10, 50);

        batch.end();

    }
    @Override
    public void show() {
        fondo = new Texture("menuprincipal.png");
    }


}
