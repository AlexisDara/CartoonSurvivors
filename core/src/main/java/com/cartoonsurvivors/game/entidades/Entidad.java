package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Entidad {
    private int vida;
    private float posicionX;
    private float posicionY;
    private float velocidad;
    private float danio;


    protected Entidad(int vida, float posicionX, float posicionY, float velocidad) {
        this.vida = vida;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.velocidad = velocidad;
        this.danio = 10.0f; // Valor predeterminado, puede ser ajustado según sea necesario
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

    public void setPosicion(float posicionX, float posicionY) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }



}
