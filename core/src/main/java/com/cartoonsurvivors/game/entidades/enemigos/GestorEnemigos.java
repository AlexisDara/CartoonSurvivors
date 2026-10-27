
package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.utils.Array;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

public class GestorEnemigos {

    private final EnemigoSpawner enemigoSpawner;
    private final Array<EnemigoBasico> enemigos = new Array<>();
    private float tiempoSpawn;
    private int enemigosMatados;

    public GestorEnemigos(EnemigoSpawner enemigoSpawner) {
        this.enemigoSpawner = enemigoSpawner;
    }

    public void actualizar(float delta, Jugador jugador) {

        tiempoSpawn += delta;
        tiempoSpawn = enemigoSpawner.aparicionEnemigos(jugador, tiempoSpawn, enemigos);


        for (int i = enemigos.size - 1; i >= 0; i--) {
            EnemigoBasico enemigo = enemigos.get(i);

            if (!enemigo.estaVivo()) {
                enemigos.removeIndex(i);
                enemigosMatados++;
                continue;
            }
            enemigo.seguirJugador(jugador.getPosicionX(), jugador.getPosicionY(), delta);
        }
    }

    public Array<EnemigoBasico> getEnemigos() {
        return enemigos;
    }

    public int getEnemigosMatados() {
        return enemigosMatados;
    }

    public void reiniciar() {
        enemigos.clear();
        tiempoSpawn = 0;
        enemigosMatados = 0;
    }
}
