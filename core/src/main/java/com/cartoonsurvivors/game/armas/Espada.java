package com.cartoonsurvivors.game.armas;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Espada extends Arma {

    public Espada() {
        super(10, 1.0f, 40f, 20f);
    }


    @Override
    public void atacar(Rectangle hitboxJugador, Vector2 direccionMirada) {
        actualizarAreaAtaque(hitboxJugador, direccionMirada);
        reiniciarAtaque();
    }
}
