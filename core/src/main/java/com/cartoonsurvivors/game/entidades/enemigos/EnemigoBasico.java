package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.g2d.Batch;

import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.ALTO_ENEMIGO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Enemigos.ANCHO_ENEMIGO;

public class EnemigoBasico extends Enemigo {
    private static final float DURACION_FOTOGRAMA = 0.05f;

    private final AparienciaEnemigo apariencia;
    private float tiempoAnimacion;

    public EnemigoBasico(int vida, float velocidad, float x, float y, AparienciaEnemigo apariencia) {
        super(vida, velocidad, x, y, ALTO_ENEMIGO / 1.5f, ANCHO_ENEMIGO / 1.5f);
        this.apariencia = apariencia;
    }

    @Override
    public void dibujar(Batch batch, float delta) {
        tiempoAnimacion += delta;
        float x = getPosicionX() + (hitbox.getWidth() - ANCHO_ENEMIGO) / 2f;
        float y = getPosicionY() + (hitbox.getHeight() - ALTO_ENEMIGO) / 2f;
        apariencia.dibujar(batch, (int) (tiempoAnimacion / DURACION_FOTOGRAMA), x, y, ANCHO_ENEMIGO, ALTO_ENEMIGO, false);
    }

    @Override
    public void dispose() {}
}
