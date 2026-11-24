package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;

final class SeleccionMejora {
    private static final float INTERVALO_REINTENTO = 0.15f;
    private static final int[] TECLAS = {Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3};

    private String partidaId;
    private int nivel;
    private int seleccion;
    private int revision;
    private float tiempoReintento;

    void actualizar(float delta, EstadisticasPartida estadisticas, ClienteUDP cliente, MenuMejoras menu) {
        var ronda = estadisticas == null ? null : estadisticas.rondaMejora();
        if (ronda == null) {
            partidaId = null;
            return;
        }

        if (!estadisticas.partidaId().equals(partidaId) || nivel != estadisticas.nivel()) {
            partidaId = estadisticas.partidaId();
            nivel = estadisticas.nivel();
            seleccion = ronda.seleccion();
            revision = ronda.revision();
        }

        int opcion = -1;
        for (int i = 0; i < TECLAS.length; i++) {
            if (Gdx.input.isKeyJustPressed(TECLAS[i])) {
                opcion = i;
                break;
            }
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            opcion = menu.obtenerOpcionMejora(Gdx.input.getX(), Gdx.input.getY(), ronda.opciones().size());
        }
        if (opcion >= 0 && opcion < ronda.opciones().size() && opcion != seleccion) {
            seleccion = opcion;
            revision++;
            tiempoReintento = 0f;
        }

        tiempoReintento -= delta;
        if (esperandoConfirmacion(estadisticas) && tiempoReintento <= 0f) {
            cliente.enviarMejora(partidaId, nivel, revision, seleccion);
            tiempoReintento = INTERVALO_REINTENTO;
        }
    }

    int seleccion() {
        return seleccion;
    }

    boolean esperandoConfirmacion(EstadisticasPartida estadisticas) {
        return revision > estadisticas.rondaMejora().revision();
    }
}
