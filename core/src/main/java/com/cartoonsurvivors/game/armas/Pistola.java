package com.cartoonsurvivors.game.armas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Pistola extends Arma {

    private static final float DURACION = 0.18f;
    // Dimensiones de la zona de impacto del servidor.
    private static final float ALCANCE = 260f;
    private static final float ANCHO = 72f;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 origen = new Vector2();
    private final Vector2 extremo = new Vector2();
    private float tiempoAnimacion;

    public Pistola() {
        super(15, 1f, ALCANCE, ANCHO, DURACION);
    }

    @Override
    public void actualizar(float delta) {
        super.actualizar(delta);
        tiempoAnimacion += delta;
    }

    @Override
    public void atacar(Rectangle hitboxJugador, Vector2 direccionMirada) {
        origen.set(hitboxJugador.x + hitboxJugador.width / 2f, hitboxJugador.y + hitboxJugador.height / 2f);
        extremo.set(direccionMirada).nor().scl(ALCANCE).add(origen);
        iniciarDisparo();
    }

    public void reproducirAtaque(float x, float y, int direccion, float alcance) {
        float angulo = direccion * 45f;
        origen.set(x, y);
        extremo.set(MathUtils.cosDeg(angulo), MathUtils.sinDeg(angulo)).scl(ALCANCE * alcance).add(origen);
        iniciarDisparo();
    }

    private void iniciarDisparo() {
        tiempoAnimacion = 0f;
        iniciarAtaque();
    }

    @Override
    public void dibujar(Batch batch) {
        if (!estaAtacando()) {
            return;
        }

        float intensidad = 1f - tiempoAnimacion / DURACION;
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.58f, 0.18f, 0.28f * intensidad);
        shapeRenderer.rectLine(origen, extremo, ANCHO);
        shapeRenderer.setColor(1f, 0.91f, 0.62f, intensidad);
        shapeRenderer.rectLine(origen, extremo, 8f * intensidad);
        shapeRenderer.circle(origen.x, origen.y, 16f * intensidad, 12);
        shapeRenderer.end();
        batch.begin();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
