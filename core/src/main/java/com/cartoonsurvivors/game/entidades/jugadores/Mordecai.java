package com.cartoonsurvivors.game.entidades.jugadores;

import com.cartoonsurvivors.game.armas.Pistola;

public class Mordecai extends Jugador {

    public Mordecai() {
        super(TipoPersonaje.MORDECAI.getRutaIdle(), "jugador/mordecai-caminando-SpriteSheet.png", new Pistola());
    }
}
