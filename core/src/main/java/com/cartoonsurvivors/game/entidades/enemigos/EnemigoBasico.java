package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.ALTO_ENEMIGO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.ANCHO_ENEMIGO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;

public class EnemigoBasico extends Enemigo {

    private Texture spriteSheet;
    private TextureRegion[] frames;
    private Animation<TextureRegion> animacionCaminar;
    private float tiempoAnimacion;

    public EnemigoBasico(int vida, float velocidad, int danio, float posicionX, float posicionY, Texture spriteSheet) {
        super(vida, velocidad, danio, posicionX, posicionY, ALTO_ENEMIGO/1.5f, ANCHO_ENEMIGO/1.5f);
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

    @Override
    public void dibujar(Batch batch, float delta) {
        float offsetX = (ANCHO_ENEMIGO - hitbox.getWidth()) / 2f;
        float offsetY = (ALTO_ENEMIGO - hitbox.getHeight()) / 2f;
            if(this.isMirandoIzquierda()) {
                if (!this.getFrameCaminar(delta).isFlipX()) {
                    this.getFrameCaminar(delta).flip(true, false);
                }
            } else {
                if (this.getFrameCaminar(delta).isFlipX()) {
                    this.getFrameCaminar(delta).flip(true, false);
                }
            }
            batch.draw(this.getFrameCaminar(delta), this.getPosicionX() - offsetX, this.getPosicionY() - offsetY, ANCHO_ENEMIGO, ALTO_ENEMIGO);

    }
}
