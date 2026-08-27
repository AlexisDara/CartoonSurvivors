package com.cartoonsurvivors.game.entidades;

public class Entidad {
    private int vida;
    private double posicionX;
    private double posicionY;

    protected Entidad(int vida, double posicionX, double posicionY) {
        this.vida = vida;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

    public void moverX(double deltaX) {
        this.posicionX += deltaX;
    }
    public void moverY(double deltaY) {
        this.posicionY += deltaY;
    }

}
