package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entidad {
    private float vida;
    private float velocidad;
    protected Rectangle hitbox;


    protected Entidad(float vida, float velocidad, float posicionX, float posicionY, float altura, float ancho) {
        this.vida = vida;
        this.velocidad = velocidad;
        this.hitbox = new Rectangle(posicionX, posicionY, ancho, altura);
    }

    public void mover(float direccionX, float direccionY, float delta) {
        hitbox.x += direccionX * velocidad * delta;
        hitbox.y += direccionY * velocidad * delta;
    }

    public float getPosicionX() {
        return hitbox.x;
    }
    public float getPosicionY() {
        return hitbox.y;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public void setPosicion(float posicionX, float posicionY) {
        this.hitbox.setPosition(posicionX, posicionY);
    }

    public float getVida() {
        return vida;
    }



    public boolean estaVivo() {
        return vida > 0;
    }

    public void setVida(float vida) {
        this.vida = vida;
    }

    public abstract void dibujar(Batch batch, float delta);

    public Rectangle getHitbox() {
        return hitbox;
    }

    public abstract void dispose();
}
