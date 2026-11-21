package com.cartoonsurvivors.game.entidades.jugadores;

import com.cartoonsurvivors.game.armas.Espada;

public class Finn extends Jugador {

    public Finn() {
        super(TipoPersonaje.FINN.getRutaIdle(), "jugador/FinIdle-Sheet.png", new Espada());
    }
}
