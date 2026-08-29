package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.cartoonsurvivors.game.entidades.Entidad;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

public abstract class Enemigo extends Entidad {
    protected Enemigo(int vida, float posicionX, float posicionY, int velocidad) {
        super(vida, posicionX, posicionY, velocidad);
    }

    public void seguirJugador(float posicionX, float posicionY) {
        float direccionX = posicionX - this.getPosicionX();
        float direccionY = posicionY - this.getPosicionY();
        // Usamos pitagoras para sacar la longitud de la distancia y normalizamos las direcciones.
        float longitud = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);
        if (longitud != 0) {
            direccionX /= longitud;
            direccionY /= longitud;
        }
        this.mover(direccionX * this.getVelocidad(), direccionY * this.getVelocidad());
    }

}
