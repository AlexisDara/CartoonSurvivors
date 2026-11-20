package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.cartoonsurvivors.game.entidades.Entidad;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

public abstract class Enemigo extends Entidad {

    private boolean mirandoIzquierda = false;

    protected Enemigo(int vida, float velocidad,float posicionX, float posicionY, float altura, float ancho) {
        super(vida,velocidad, posicionX, posicionY, altura, ancho);
    }


    public boolean isMirandoIzquierda() {
        return mirandoIzquierda;
    }

}
