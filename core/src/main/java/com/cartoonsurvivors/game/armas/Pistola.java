package com.cartoonsurvivors.game.armas;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Pistola extends Arma {
    public Pistola() {
        super(15, 1f, 0f, 0f, 0f);
    }

    @Override
    public void atacar(Rectangle hitboxJugador, Vector2 direccionMirada) {
        // Las balas se crean en el servidor.
    }

    @Override
    public void dibujar(Batch batch) {
        // Las balas se dibujan a partir del estado del mundo.
    }
}
