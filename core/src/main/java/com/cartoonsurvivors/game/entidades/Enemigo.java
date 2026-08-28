package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;

public abstract class Enemigo extends Entidad {
    protected Enemigo(int vida, float posicionX, float posicionY, int velocidad, Texture textura) {
        super(vida, posicionX, posicionY, velocidad, textura);
    }

    public void seguirJugador(Jugador jugador){
        float direccionX = jugador.getPosicionX() - this.getPosicionX();
        float direccionY = jugador.getPosicionY() - this.getPosicionY();

        // Usamos pitagoras para sacar la longitud de la distancia y normalizamos las direcciones.
        float longitud = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);
        if (longitud != 0) {
            direccionX /= longitud;
            direccionY /= longitud;
        }

        this.mover(direccionX * this.getVelocidad(), direccionY * this.getVelocidad());
    }

}
