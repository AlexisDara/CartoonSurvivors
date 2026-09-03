package com.cartoonsurvivors.game.entidades.jugadores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cartoonsurvivors.game.entidades.Entidad;
import com.cartoonsurvivors.game.utilidades.Constantes;

import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;

public abstract class Jugador extends Entidad {

    private Texture texturaIdle;
    private Animation<TextureRegion> animacionCaminar;
    private float tiempoAnimacion = 0;
    private boolean mirandoDerecha = false;
    private boolean seEstaMoviendo = false;

    public Jugador(Texture texturaIdle, Animation<TextureRegion> animacionCaminar) {
        super(Constantes.Jugador.VIDA_INICIAL, Constantes.Jugador.VELOCIDAD_INICIAL, 0, 0, 0, 60f, 40f);
        this.texturaIdle = texturaIdle;
        this.animacionCaminar = animacionCaminar;
    }

    public Texture getTexturaIdle() {
        return texturaIdle;
    }
    public Animation<TextureRegion> getAnimacionCaminar() {
        return animacionCaminar;
    }

    public TextureRegion getFrameCaminar(float delta) {
        tiempoAnimacion += delta;
        return animacionCaminar.getKeyFrame(tiempoAnimacion, true);
    }

    public void seMueve(float direccionX, float direccionY) {
        this.seEstaMoviendo = direccionX != 0 || direccionY != 0;
    }

    public void calcularLadoMirada(float direccionX) {
        if (direccionX < 0) {
            this.mirandoDerecha = false;
        }

        if (direccionX > 0) {
            this.mirandoDerecha = true;
        }
    }

    public boolean getSeEstaMoviendo() {
        return seEstaMoviendo;
    }

    public boolean estaMirandoDerecha() {
        return mirandoDerecha;
    }

    @Override
    public void dibujar(Batch batch, float delta) {
        float offsetX = (TAMAÑO_REAL - hitbox.getWidth()) / 2f;
        float offsetY = (TAMAÑO_REAL - hitbox.getHeight()) / 2f;
        if (seEstaMoviendo) {
            TextureRegion frame = this.getFrameCaminar(delta);

            if (this.estaMirandoDerecha()) {
                if (!frame.isFlipX()) {
                    frame.flip(true, false);
                }
            } else {
                if (frame.isFlipX()) {
                    frame.flip(true, false);
                }
            }
            batch.draw(frame, this.getPosicionX() - offsetX, this.getPosicionY() - offsetY, TAMAÑO_REAL, TAMAÑO_REAL);
        } else {
            batch.draw(this.getTexturaIdle(), this.getPosicionX() - offsetX, this.getPosicionY() - offsetY, TAMAÑO_REAL, TAMAÑO_REAL);
        }
    }


}
