package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class PantallaPausa {
    private final Stage stage;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final ExtendViewport viewport;

    private Runnable onContinuar;
    private Runnable onReiniciar;
    private Runnable onMenuPrincipal;

    public PantallaPausa(CartoonSurvivors game) {
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(Constantes.Mundo.ANCHO_INTERFAZ, Constantes.Mundo.ALTO_INTERFAZ, camera);
        this.stage = new Stage(viewport);
        this.shapeRenderer = new ShapeRenderer();
        configurarUI(game);
    }

    private void configurarUI(CartoonSurvivors game) {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.getFont();
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.ORANGE;

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton btnContinuar = new TextButton("CONTINUAR", buttonStyle);
        TextButton btnReiniciar = new TextButton("REINICIAR", buttonStyle);
        TextButton btnMenuPrincipal = new TextButton("MENÚ PRINCIPAL", buttonStyle);

        btnContinuar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onContinuar != null) onContinuar.run();
            }
        });

        btnReiniciar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onReiniciar != null) onReiniciar.run();
            }
        });

        btnMenuPrincipal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onMenuPrincipal != null) onMenuPrincipal.run();
            }
        });

        table.add(btnContinuar).width(300).height(70).padBottom(30);
        table.row();
        table.add(btnReiniciar).width(300).height(70).padBottom(30);
        table.row();
        table.add(btnMenuPrincipal).width(300).height(70);
    }

    public void render(float delta) {
        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.1f); // 10% opacidad
        shapeRenderer.rect(0, 0, Constantes.Mundo.ANCHO_INTERFAZ, Constantes.Mundo.ALTO_INTERFAZ);
        shapeRenderer.end();

        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }

    public Stage getStage() { return stage; }
    public void setOnContinuar(Runnable onContinuar) { this.onContinuar = onContinuar; }
    public void setOnReiniciar(Runnable onReiniciar) { this.onReiniciar = onReiniciar; }
    public void setOnMenuPrincipal(Runnable onMenuPrincipal) { this.onMenuPrincipal = onMenuPrincipal; }
}
