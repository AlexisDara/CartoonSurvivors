
package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.utils.Array;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.DANIO_ENEMIGO;

public class SistemaColisiones {

    public void comprobarColisionesJugador(
        Jugador jugador,
        Array<EnemigoBasico> enemigos,
        float delta
    ) {
        int numeroHits = 0;

        for (EnemigoBasico enemigo : enemigos) {
            if (jugador.colisionaCon(enemigo)) {
                numeroHits++;
            }
        }

        if (numeroHits > 0) {
            jugador.recibirDanio(numeroHits * DANIO_ENEMIGO * delta);
        }
    }

    public void comprobarAtaques(
        Jugador jugador,
        Array<EnemigoBasico> enemigos
    ) {
        if (!jugador.getArma().estaAtacando()) {
            return;
        }

        for (EnemigoBasico enemigo : enemigos) {
            jugador.getArma().comprobarGolpe(enemigo);
        }
    }
}
