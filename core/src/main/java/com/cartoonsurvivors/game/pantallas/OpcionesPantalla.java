package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class OpcionesPantalla extends ScreenAdapter {

    private final CartoonSurvivors game;
    private Stage stage;
    private Texture fondo;
    private TextButtonStyle buttonStyle;
    private Label.LabelStyle labelStyle;

    private float volumenMusica;
    private float volumenSonidoEfecto;

    public OpcionesPantalla(CartoonSurvivors game) {
        this.game = game;
        this.volumenMusica = game.getAudioManager().getVolumenMusica();
        this.volumenSonidoEfecto = game.getAudioManager().getVolumenSonidoEfecto();
    }

    @Override
    public void show() {
        fondo = new Texture("menuprincipal.png");
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        buttonStyle = new TextButtonStyle();
        buttonStyle.font = game.getFont();
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.ORANGE;

        labelStyle = new Label.LabelStyle();
        labelStyle.font = game.getFont();
        labelStyle.fontColor = Color.WHITE;

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        Label tituloLabel = new Label("Opciones", labelStyle);
        table.add(tituloLabel).padBottom(20).row();

        Label volumenMusicaLabel = new Label("Volumen Música: " + (int)(volumenMusica * 100) + "%", labelStyle);
        TextButton musicaMenos = new TextButton("-", buttonStyle);
        TextButton musicaMas = new TextButton("+", buttonStyle);

        TextButton btnVolver = new TextButton("Volver", buttonStyle);

        musicaMenos.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                volumenMusica = Math.max(0, volumenMusica - 0.1f);
                game.getAudioManager().setVolumenMusica(volumenMusica);
                volumenMusicaLabel.setText("Volumen Música: " + (int)(volumenMusica * 100) + "%");
            }
        });
        musicaMas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                volumenMusica = Math.min(1, volumenMusica + 0.1f);
                game.getAudioManager().setVolumenMusica(volumenMusica);
                volumenMusicaLabel.setText("Volumen Música: " + (int)(volumenMusica * 100) + "%");
            }
        });

        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuPantalla(game));
            }
        });

        table.add(tituloLabel).colspan(3).padBottom(30);
        table.row();
        table.add(volumenMusicaLabel).left();
        table.add(musicaMenos).width(60).height(60).padLeft(10);
        table.add(musicaMas).width(60).height(60).padLeft(10);
        table.row().padTop(10);
        table.add(btnVolver).colspan(3).width(300).height(70);

    }

    @Override
    public void render(float delta) {
        stage.getBatch().begin();
        stage.getBatch().draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        fondo.dispose();
    }
}
