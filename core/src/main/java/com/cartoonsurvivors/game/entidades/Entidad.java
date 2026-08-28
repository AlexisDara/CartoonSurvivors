package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Entidad {
    private int vida;
    private float posicionX;
    private float posicionY;
    private int velocidad;


    protected Entidad(int vida, float posicionX, float posicionY, int velocidad) {
        this.vida = vida;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.velocidad = velocidad;
    }

    public void mover(float deltaX, float deltaY) {
        this.posicionX += deltaX;
        this.posicionY += deltaY;

    }

    public float getPosicionX() {
        return posicionX;
    }

    public float getPosicionY() {
        return posicionY;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public void setPosicion(int posicionX, int posicionY) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }



}
