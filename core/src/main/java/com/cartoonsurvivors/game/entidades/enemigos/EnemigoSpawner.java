package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.MARGEN_APARICION;
import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.TIEMPO_APARICION;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_MUNDO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_MUNDO;

public class EnemigoSpawner {

    private int vida;
    private float velocidad;
    private int danio;
    private Texture spriteSheet;

    public EnemigoSpawner(int vida, float velocidad, int danio, Texture spriteSheet) {
        this.vida = vida;
        this.velocidad = velocidad;
        this.danio = danio;
        this.spriteSheet = spriteSheet;
    }

    public EnemigoBasico spawn(float posicionX, float posicionY) {
        return new EnemigoBasico(vida, velocidad, danio, posicionX, posicionY, spriteSheet);
    }

    public float aparicionEnemigos(Jugador jugador, float tiempoUltimoSpawn, Array<EnemigoBasico> enemigos) {
        if (tiempoUltimoSpawn >= TIEMPO_APARICION) {
            tiempoUltimoSpawn = 0;
            float posicionX;
            float posicionY;
            int lado = MathUtils.random(3);

            switch (lado) {

                case 0: // Arriba
                    posicionX = jugador.getPosicionX() + MathUtils.random(-ANCHO_MUNDO / 2f, ANCHO_MUNDO / 2f);
                    posicionY = jugador.getPosicionY() + ALTO_MUNDO / 2f + MARGEN_APARICION;
                    break;
                case 1: // Abajo
                    posicionX = jugador.getPosicionX() + MathUtils.random(-ANCHO_MUNDO / 2f, ANCHO_MUNDO / 2f);
                    posicionY = jugador.getPosicionY() - ALTO_MUNDO / 2f - MARGEN_APARICION;
                    break;
                case 2: // Izquierda
                    posicionX = jugador.getPosicionX() - ANCHO_MUNDO / 2f - MARGEN_APARICION;
                    posicionY = jugador.getPosicionY() + MathUtils.random(-ALTO_MUNDO / 2f, ALTO_MUNDO / 2f);
                    break;
                default: // Derecha
                    posicionX = jugador.getPosicionX() + ANCHO_MUNDO / 2f + MARGEN_APARICION;
                    posicionY = jugador.getPosicionY() + MathUtils.random(-ALTO_MUNDO / 2f, ALTO_MUNDO / 2f);
                    break;
            }
            EnemigoBasico enemigo = this.spawn(posicionX, posicionY );
            enemigos.add(enemigo);
        }
        return tiempoUltimoSpawn;
    }

}
