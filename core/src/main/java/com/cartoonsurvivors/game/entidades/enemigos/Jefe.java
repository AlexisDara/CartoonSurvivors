package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Jefe extends Enemigo {

    private static final int TAMANO_FRAME = 128;
    private static final float TAMANO_SPRITE = 130f;
    private static final float ANCHO_HITBOX = 84f;
    private static final float ALTO_HITBOX = 94f;

    private final Animation<TextureRegion> animacionCaminar;
    private boolean mirandoIzquierda;
    private float tiempoAnimacion;

    public Jefe(int vida, float x, float y, Texture spriteSheet) {
        super(vida, 78f, x, y, ALTO_HITBOX, ANCHO_HITBOX);
        animacionCaminar = new Animation<>(0.12f, TextureRegion.split(spriteSheet, TAMANO_FRAME, TAMANO_FRAME)[0]);
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

    @Override
    public void dispose() {}
}
