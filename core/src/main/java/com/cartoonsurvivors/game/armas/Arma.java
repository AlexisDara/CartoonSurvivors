package com.cartoonsurvivors.game.armas;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Arma {

    private int danio;
    private float tiempoEntreAtaques;
    private float tiempoDesdeUltimoAtaque;

    private float tiempoAtaque;
    private float duracionAtaque;
    private boolean estaAtacando;

    private final Rectangle areaAtaque = new Rectangle();

    protected Arma(
        int danio,
        float tiempoEntreAtaques,
        float anchoAtaque,
        float altoAtaque,
        float duracionAtaque
    ) {
        this.danio = danio;
        this.tiempoEntreAtaques = tiempoEntreAtaques;
        this.tiempoDesdeUltimoAtaque = tiempoEntreAtaques;

        this.duracionAtaque = duracionAtaque;
        this.tiempoAtaque = 0;
        this.estaAtacando = false;

        this.areaAtaque.setSize(anchoAtaque, altoAtaque);
    }

    public void actualizar(float delta) {
        tiempoDesdeUltimoAtaque += delta;

        if (estaAtacando) {
            tiempoAtaque += delta;

            if (tiempoAtaque >= duracionAtaque) {
                estaAtacando = false;
                tiempoAtaque = 0;
            }
        }
    }

    public boolean puedeAtacar() {
        return tiempoDesdeUltimoAtaque >= tiempoEntreAtaques;
    }

    protected void iniciarAtaque() {
        estaAtacando = true;
        tiempoAtaque = 0;
        reiniciarAtaque();
    }

    public boolean estaAtacando() {
        return estaAtacando;
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

    public Rectangle getAreaAtaque() {
        return areaAtaque;
    }

    public void actualizarAreaAtaque(
        Rectangle hitboxJugador,
        Vector2 direccionMirada
    ) {
        float centroX = hitboxJugador.x + hitboxJugador.width / 2f;
        float centroY = hitboxJugador.y + hitboxJugador.height / 2f;

        float distancia = 50f;

        float centroAtaqueX =
            centroX + direccionMirada.x * distancia;

        float centroAtaqueY =
            centroY + direccionMirada.y * distancia;

        areaAtaque.set(
            centroAtaqueX - areaAtaque.width / 2f,
            centroAtaqueY - areaAtaque.height / 2f,
            areaAtaque.width,
            areaAtaque.height
        );
    }

    public abstract void atacar(
        Rectangle hitboxJugador,
        Vector2 direccionMirada
    );

    public abstract void dibujar(Batch batch);
}
