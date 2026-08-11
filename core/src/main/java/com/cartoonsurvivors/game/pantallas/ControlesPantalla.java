package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.cartoonsurvivors.game.CartoonSurvivors;

import java.awt.*;

public class ControlesPantalla extends ScreenAdapter {
    private final CartoonSurvivors game;
    private final Batch batch;
    private final BitmapFont font;
    private final ExtendViewport viewport = new ExtendViewport(800, 480, camera);

    public ControlesPantalla(CartoonSurvivors game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
    }
}
