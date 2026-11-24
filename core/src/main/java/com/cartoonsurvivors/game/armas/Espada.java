package com.cartoonsurvivors.game.armas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Espada extends Arma {

    private static final float DURACION = 0.20f;

    private static final float RADIO_INTERNO = 37f;
    private static final float RADIO_EXTERNO = 184f;

    private static final float ANGULO_BARRIDO = 110f;
    private static final int SEGMENTOS = 20;

    private final ShapeRenderer shapeRenderer;
    private final Vector2 direccionAtaque = new Vector2(1f, 0f);

    private Rectangle hitboxJugador;
    private float tiempoAnimacion;
    private float alcanceEstela = 1f;

    public Espada() {
        super(24, 1.0f, 80f, 30f, DURACION);

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void actualizar(float delta) {

        super.actualizar(delta);

        if (estaAtacando()) {
            tiempoAnimacion += delta;
        }
    }

    @Override
    public void atacar(Rectangle hitbox, Vector2 direccionMirada) {

        alcanceEstela = 1f;
        hitboxJugador = hitbox;

        direccionAtaque.set(direccionMirada).nor();

        actualizarAreaAtaque(hitbox, direccionAtaque);

        tiempoAnimacion = 0f;

        iniciarAtaque();
    }

    public void reproducirAtaque(Rectangle hitbox, int direccion) {
        reproducirAtaque(hitbox, direccion, 1f);
    }

    public void reproducirAtaque(Rectangle hitbox, int direccion, float alcance) {

        float angulo = direccion * 45f;

        Vector2 vector = new Vector2(MathUtils.cosDeg(angulo), MathUtils.sinDeg(angulo));

        atacar(hitbox, vector);
        alcanceEstela = alcance;
    }

    @Override
    public void dibujar(Batch batch) {

        if (!estaAtacando() || hitboxJugador == null) {
            return;
        }

        float progreso = MathUtils.clamp(tiempoAnimacion / DURACION, 0f, 1f);

        float centroX = hitboxJugador.x + hitboxJugador.width / 2f;

        float centroY = hitboxJugador.y + hitboxJugador.height / 2f;

        float anguloBase = direccionAtaque.angleDeg();

        // La estela se extiende progresivamente.
        float extension = MathUtils.clamp(progreso / 0.55f, 0f, 1f);

        float desvanecimiento = 1f;

        if (progreso > 0.55f) {
            desvanecimiento = 1f - (progreso - 0.55f) / 0.45f;
        }

        float inicio = anguloBase - ANGULO_BARRIDO / 2f;

        float fin = inicio + ANGULO_BARRIDO * extension;

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Parte exterior celeste de la estela.
        dibujarEstela(centroX, centroY, inicio, fin, RADIO_INTERNO, RADIO_EXTERNO, new Color(0.35f, 0.8f, 1f, 0.65f * desvanecimiento));

        // Franja brillante interior.
        dibujarEstela(centroX, centroY, inicio, fin, RADIO_EXTERNO - 26f, RADIO_EXTERNO - 6f, new Color(0.9f, 1f, 1f, 0.9f * desvanecimiento));

        shapeRenderer.end();

        batch.begin();
    }

    private void dibujarEstela(float centroX, float centroY, float inicio, float fin, float radioInterno, float radioExterno, Color color) {

        radioInterno *= alcanceEstela;
        radioExterno *= alcanceEstela;
        shapeRenderer.setColor(color);

        for (int i = 0; i < SEGMENTOS; i++) {

            float t1 = (float) i / SEGMENTOS;
            float t2 = (float) (i + 1) / SEGMENTOS;

            float a1 = MathUtils.lerp(inicio, fin, t1);
            float a2 = MathUtils.lerp(inicio, fin, t2);

            // Afinar la estela en sus extremos.
            float grosor1 = MathUtils.sin(t1 * MathUtils.PI);
            float grosor2 = MathUtils.sin(t2 * MathUtils.PI);

            float r1 = MathUtils.lerp(radioExterno, radioInterno, grosor1);

            float r2 = MathUtils.lerp(radioExterno, radioInterno, grosor2);

            float x1 = centroX + MathUtils.cosDeg(a1) * radioExterno;
            float y1 = centroY + MathUtils.sinDeg(a1) * radioExterno;

            float x2 = centroX + MathUtils.cosDeg(a2) * radioExterno;
            float y2 = centroY + MathUtils.sinDeg(a2) * radioExterno;

            float x3 = centroX + MathUtils.cosDeg(a1) * r1;
            float y3 = centroY + MathUtils.sinDeg(a1) * r1;

            float x4 = centroX + MathUtils.cosDeg(a2) * r2;
            float y4 = centroY + MathUtils.sinDeg(a2) * r2;

            shapeRenderer.triangle(x1, y1, x3, y3, x2, y2);

            shapeRenderer.triangle(x2, y2, x3, y3, x4, y4);
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
