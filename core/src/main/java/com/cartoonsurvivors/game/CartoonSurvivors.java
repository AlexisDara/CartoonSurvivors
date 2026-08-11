package com.cartoonsurvivors.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cartoonsurvivors.game.pantallas.ControlesPantalla;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class CartoonSurvivors extends Game {
    private Batch batch;
    private BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/CherryCreamSoda-Regular.ttf"));
        var parametrosLetra = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametrosLetra.size = 30;
        parametrosLetra.color = Color.BLACK;
        font = generator.generateFont(parametrosLetra);
        generator.dispose();

        setScreen(new ControlesPantalla());
    }

    @Override
    public void render() {
    }


    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();
    }

    public Batch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }
}
