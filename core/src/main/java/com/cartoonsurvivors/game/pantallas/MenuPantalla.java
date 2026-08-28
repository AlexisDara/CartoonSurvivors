package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class MenuPantalla extends ScreenAdapter {
    private final CartoonSurvivors game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private Texture fondo;
    private Stage stage;

    public MenuPantalla(CartoonSurvivors game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(Constantes.Mundo.ANCHO_INTERFAZ, Constantes.Mundo.ALTO_INTERFAZ, camera);
    }

    @Override
    public void show() {
        fondo = new Texture("menuprincipal.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.ORANGE;

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton btnJugar = new TextButton("JUGAR", buttonStyle);
        TextButton btnSalir = new TextButton("SALIR", buttonStyle);

        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new JuegoPantalla(game.getBatch()));
            }
        });

        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        table.add(btnJugar).width(300).height(70).padBottom(40);
        table.row();
        table.add(btnSalir).width(300).height(70);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(fondo, 0, 0, Constantes.Mundo.ANCHO_INTERFAZ, Constantes.Mundo.ALTO_INTERFAZ);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (fondo != null) {
            fondo.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }
}
