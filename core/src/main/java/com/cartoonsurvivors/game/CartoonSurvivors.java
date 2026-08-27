package com.cartoonsurvivors.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.cartoonsurvivors.game.pantallas.JuegoPantalla;
import com.cartoonsurvivors.game.pantallas.MenuPantalla;
import static com.cartoonsurvivors.game.utilidades.Constantes.Recursos.*;



/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class CartoonSurvivors extends Game {
    private SpriteBatch batch;
    private BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        var generator = new FreeTypeFontGenerator(Gdx.files.internal(RUTA_FUENTE));
        var parametrosLetra = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametrosLetra.size = 32;
        parametrosLetra.color = Color.WHITE;
        font = generator.generateFont(parametrosLetra);
        generator.dispose();

        setScreen(new JuegoPantalla(this.batch));
    }

    @Override
    public void render() {
        super.render();
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
