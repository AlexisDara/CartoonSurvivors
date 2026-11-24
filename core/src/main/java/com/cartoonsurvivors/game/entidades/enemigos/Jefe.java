package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Jefe extends Enemigo {

    private static final float TAMANO_SPRITE = 130f;
    private static final float ANCHO_HITBOX = 84f;
    private static final float ALTO_HITBOX = 94f;
    private static final float ANCHO_BARRA = 84f;
    private static final float ALTO_BARRA = 8f;

    private final Animation<TextureRegion> animacionCaminar;
    private boolean mirandoIzquierda;
    private float tiempoAnimacion;
    private int vidaMaxima;

    public Jefe(int vida, float x, float y, Texture spriteSheet, int anchoFrame) {
        super(vida, 78f, x, y, ALTO_HITBOX, ANCHO_HITBOX);
        int anchoTextura = spriteSheet.getWidth();
        TextureRegion[] frames = new TextureRegion[(anchoTextura + anchoFrame - 1) / anchoFrame];
        for (int i = 0; i < frames.length; i++) {
            int origenX = i * anchoFrame;
            int anchoVisible = Math.min(anchoFrame, anchoTextura - origenX);
            frames[i] = new TextureRegion(spriteSheet, origenX, 0, anchoVisible, spriteSheet.getHeight());
        }
        animacionCaminar = new Animation<>(0.12f, frames);
    }

    @Override
    public void setPosicion(float x, float y) {
        if (x != getPosicionX()) {
            mirandoIzquierda = x < getPosicionX();
        }
        super.setPosicion(x, y);
    }

    @Override
    public boolean isMirandoIzquierda() {
        return mirandoIzquierda;
    }

    @Override
    public void dibujar(Batch batch, float delta) {
        tiempoAnimacion += delta;
        TextureRegion frame = animacionCaminar.getKeyFrame(tiempoAnimacion, true);
        if (frame.isFlipX() != isMirandoIzquierda()) {
            frame.flip(true, false);
        }
        float x = getPosicionX() + (ANCHO_HITBOX - TAMANO_SPRITE) / 2f;
        float y = getPosicionY() + (ALTO_HITBOX - TAMANO_SPRITE) / 2f;
        batch.draw(frame, x, y, TAMANO_SPRITE, TAMANO_SPRITE);
    }

    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
    }

    public void dibujarBarraVida(Batch batch, Texture pixel) {
        if (vidaMaxima <= 0) {
            return;
        }
        float x = getPosicionX() + (ANCHO_HITBOX - ANCHO_BARRA) / 2f;
        float y = getPosicionY() + (ALTO_HITBOX + TAMANO_SPRITE) / 2f + 4f;
        float colorAnterior = batch.getPackedColor();
        batch.setColor(0.15f, 0.08f, 0.10f, 1f);
        batch.draw(pixel, x, y, ANCHO_BARRA, ALTO_BARRA);
        batch.setColor(0.88f, 0.19f, 0.20f, 1f);
        batch.draw(pixel, x + 1f, y + 1f, (ANCHO_BARRA - 2f) * getVida() / vidaMaxima, ALTO_BARRA - 2f);
        batch.setPackedColor(colorAnterior);
    }

    @Override
    public void dispose() {}
}
