package com.cartoonsurvivors.game.entidades.jugadores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.entidades.Entidad;
import com.cartoonsurvivors.game.utilidades.Constantes;

import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;

public abstract class Jugador extends Entidad {

    private Texture texturaIdle;
    private Animation<TextureRegion> animacionCaminar;
    private float tiempoAnimacion = 0;
    private boolean mirandoDerecha = false;
    private boolean seEstaMoviendo = false;
    private final Vector2 direccionMirada = new Vector2(1, 0);
    private Arma arma;

    public Jugador(Texture texturaIdle, Animation<TextureRegion> animacionCaminar) {
        super(Constantes.Jugador.VIDA_INICIAL, Constantes.Jugador.VELOCIDAD_INICIAL, 0, 0, 0, 60f, 40f);
        this.texturaIdle = texturaIdle;
        this.animacionCaminar = animacionCaminar;
        this.arma = new Espada();
    }

    public Arma getArma() {
        return arma;
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
        if (direccionX != 0 || direccionY != 0) {
            this.seEstaMoviendo = true;
            direccionMirada.set(direccionX, direccionY).nor();
        } else {
            this.seEstaMoviendo = false;
        }
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

    public void actualizarArma(float delta) {
        arma.actualizar(delta);

        if (arma.puedeAtacar()) {
            arma.atacar(this.hitbox, this.direccionMirada);
        }
    }



    @Override public void dibujar(Batch batch, float delta) {
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
            batch.draw( frame, this.getPosicionX() - offsetX, this.getPosicionY() - offsetY, TAMAÑO_REAL, TAMAÑO_REAL );
        } else {
            TextureRegion idle = new TextureRegion(this.getTexturaIdle());
            if (this.estaMirandoDerecha()) {
                idle.flip(true, false);
    }

    batch.draw( idle, this.getPosicionX() - offsetX, this.getPosicionY() - offsetY, TAMAÑO_REAL, TAMAÑO_REAL );

    }

    // DIBUJAR EL ATAQUE
      arma.dibujar(batch);
    }


}
