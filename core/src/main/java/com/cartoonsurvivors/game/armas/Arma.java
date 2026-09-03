package com.cartoonsurvivors.game.armas;

public abstract class Arma {

    private int danio;
    private float tiempoEntreAtaques;
    private float tiempoDesdeUltimoAtaque;

    protected Arma(int danio, float tiempoEntreAtaques) {
        this.danio = danio;
        this.tiempoEntreAtaques = tiempoEntreAtaques;
        this.tiempoDesdeUltimoAtaque = tiempoEntreAtaques;
    }

    public void actualizar(float delta) {
        tiempoDesdeUltimoAtaque += delta;
    }

    public boolean puedeAtacar() {
        return tiempoDesdeUltimoAtaque >= tiempoEntreAtaques;
    }

    public void reiniciarAtaque() {
        tiempoDesdeUltimoAtaque = 0;
    }

    public int getDanio() {
        return danio;
    }

    public float getTiempoEntreAtaques() {
        return tiempoEntreAtaques;
    }
}
