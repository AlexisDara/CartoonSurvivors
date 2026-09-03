package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.cartoonsurvivors.game.entidades.Entidad;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

public abstract class Enemigo extends Entidad {
    private boolean mirandoIzquierda = false;
    protected Enemigo(int vida, float posicionX, float posicionY, float velocidad, int danio) {
        super(vida, posicionX, posicionY, velocidad, danio);
    }

    public void seguirJugador(float posicionX, float posicionY, float delta) {
        float direccionX = posicionX - this.getPosicionX();
        float direccionY = posicionY - this.getPosicionY();
        // Usamos pitagoras para sacar la longitud de la distancia y normalizamos las direcciones.
        float longitud = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);
        if (longitud != 0) {
            direccionX /= longitud;
            direccionY /= longitud;
        }
        if(direccionX < 0) {
            mirandoIzquierda = true;
        } else if(direccionX > 0) {
            mirandoIzquierda = false;
        }
        this.mover(direccionX , direccionY ,delta);

    }
    public boolean isMirandoIzquierda() {
        return mirandoIzquierda;
    }

}
