package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EnemigoBasico extends Enemigo {

    private Texture spriteSheet;
    private TextureRegion[] frames;
    private Animation<TextureRegion> animacionCaminar;
    private float tiempoAnimacion;

    public EnemigoBasico(int vida, float posicionX, float posicionY, int velocidad, Texture spriteSheet) {
        super(vida, posicionX, posicionY, velocidad);
        this.spriteSheet = spriteSheet;
        this.frames = TextureRegion.split(spriteSheet, 48, 80)[0];
        this.animacionCaminar = new Animation<>(0.1f, frames);

    }
    public TextureRegion getFrameCaminar(float delta) {
        tiempoAnimacion += delta;
        return animacionCaminar.getKeyFrame(tiempoAnimacion, true);
    }

    public Animation<TextureRegion> getAnimacionCaminar() {
        return animacionCaminar;
    }
}
