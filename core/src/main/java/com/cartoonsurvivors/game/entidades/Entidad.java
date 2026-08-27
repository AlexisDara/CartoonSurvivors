package com.cartoonsurvivors.game.entidades;

public class Entidad {
    private int vida;
    private int posicionX;
    private int posicionY;

    protected Entidad(int vida, int posicionX, int posicionY) {
        this.vida = vida;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

    public void moverX(float deltaX) {
        this.posicionX += deltaX;
    }

    public int getPosicionX() {
        return posicionX;
    }

    public int getPosicionY() {
        return posicionY;
    }

    public void moverY(float deltaY) {
        this.posicionY += deltaY;
    }

}
