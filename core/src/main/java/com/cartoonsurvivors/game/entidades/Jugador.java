package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class Jugador extends Entidad {

    public Jugador(float posicionX, float posicionY, Texture textura) {
        super(Constantes.Jugador.vidaInicial, posicionX, posicionY, Constantes.Jugador.velocidadInicial, textura);
    }

}
