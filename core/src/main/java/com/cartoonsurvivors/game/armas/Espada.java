package com.cartoonsurvivors.game.armas;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Espada extends Arma {

    private final ShapeRenderer shapeRenderer;
    private final Vector2 direccionAtaque = new Vector2();
    public Espada() {
        super(25, 1.0f, 40f, 20f, 0.2f);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void atacar(
        Rectangle hitboxJugador,
        Vector2 direccionMirada
    ) {
        direccionAtaque.set(direccionMirada);

        actualizarAreaAtaque(hitboxJugador, direccionMirada);

        iniciarAtaque();
    }

    @Override
    public void dibujar(Batch batch) {

        if (!estaAtacando()) {
            return;
        }

        Rectangle area = getAreaAtaque();

        float centroX = area.x + area.width / 2f;
        float centroY = area.y + area.height / 2f;

        float angulo = direccionAtaque.angleDeg();

        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.arc(
            centroX,
            centroY,
            30f,
            angulo - 45f,
            90f
        );

        shapeRenderer.end();

        batch.begin();
    }
}
