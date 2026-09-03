package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entidad {
    private int vida;
    private float velocidad;
    private int danio;
    private Rectangle hitbox;


    protected Entidad(int vida, float velocidad, int danio, float posicionX, float posicionY, float altura, float ancho) {
        this.vida = vida;
        this.velocidad = velocidad;
        this.danio = danio;
        this.hitbox = new Rectangle(posicionX, posicionY, ancho, altura);
    }

    public void mover(float direccionX, float direccionY, float delta) {
        float movimientoX = direccionX * delta * this.velocidad;
        float movimientoY = direccionY * delta * this.velocidad;
        hitbox.setPosition(hitbox.x + movimientoX, hitbox.y + movimientoY);
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

    public int getVida() {
        return vida;
    }

    public int getDanio() {
        return danio;
    }

    public void recibirDanio(int danio) {
        vida -= danio;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public boolean colisionaCon(Entidad entidad) {
        return hitbox.overlaps(entidad.hitbox);
    }

    public abstract void dibujar(Batch batch, float delta);






}
