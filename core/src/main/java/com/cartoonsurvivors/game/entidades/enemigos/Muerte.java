package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Muerte extends Enemigo {

    private static final int TAMANO_FRAME = 64;
    private static final float TAMANO_SPRITE = 176f;
    private static final float ANCHO_HITBOX = 112f;
    private static final float ALTO_HITBOX = 132f;

    private final Animation<TextureRegion> caminar;
    private float tiempoAnimacion;
    private boolean mirandoIzquierda;

    public Muerte(int vida, float x, float y, Texture textura) {
        super(vida, 650f, x, y, ALTO_HITBOX, ANCHO_HITBOX);
        caminar = new Animation<>(0.075f, TextureRegion.split(textura, TAMANO_FRAME, TAMANO_FRAME)[0]);
    }

    @Override
    public void setPosicion(float x, float y) {
        if (x != getPosicionX()) {
            mirandoIzquierda = x < getPosicionX();
        }
        super.setPosicion(x, y);
    }

    @Override
    public void dibujar(Batch batch, float delta) {
        tiempoAnimacion += delta;
        TextureRegion frame = caminar.getKeyFrame(tiempoAnimacion, true);
        if (frame.isFlipX() != mirandoIzquierda) {
            frame.flip(true, false);
        }
        batch.draw(frame, getPosicionX() + (ANCHO_HITBOX - TAMANO_SPRITE) / 2f,
            getPosicionY() + (ALTO_HITBOX - TAMANO_SPRITE) / 2f, TAMANO_SPRITE, TAMANO_SPRITE);
    }

    @Override
    public void dispose() {}
}
