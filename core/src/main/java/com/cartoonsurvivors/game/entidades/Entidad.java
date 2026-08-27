package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Entidad {
    private int vida;
    private float posicionX;
    private float posicionY;
    private int velocidad;
    private Sprite sprite;


    protected Entidad(int vida, float posicionX, float posicionY, int velocidad, Texture textura) {
        this.vida = vida;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.velocidad = velocidad;
        this.sprite = new Sprite(textura);
    }

    public void moverX(float deltaX) {
        this.posicionX += deltaX;
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

    public void moverY(float deltaY) {
        this.posicionY += deltaY;
    }

}
