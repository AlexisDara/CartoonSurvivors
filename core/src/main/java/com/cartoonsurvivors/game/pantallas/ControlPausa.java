package com.cartoonsurvivors.game.pantallas;

import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;

final class ControlPausa {
    private static final float INTERVALO_REINTENTO = 0.15f;

    private Solicitud pendiente;
    private float tiempoReintento;

    void solicitar(EstadisticasPartida estado, boolean reanudar) {
        if (estado == null || estado.pausa() == null || estado.pausa().activa() != reanudar) {
            return;
        }
        pendiente = new Solicitud(estado.partidaId(), estado.pausa().revision(), reanudar);
        tiempoReintento = 0f;
    }

    void actualizar(float delta, EstadisticasPartida estado, int idLocal, ClienteUDP cliente) {
        if (pendiente == null) {
            return;
        }
        if (estado == null || estado.pausa() == null || !pendiente.partidaId().equals(estado.partidaId())
            || pendiente.revision() != estado.pausa().revision()
            || pendiente.reanudar() != estado.pausa().activa() || estado.pausa().confirmado(idLocal)) {
            pendiente = null;
            return;
        }

        tiempoReintento -= delta;
        if (tiempoReintento <= 0f) {
            cliente.enviarPausa(pendiente.partidaId(), pendiente.revision(), pendiente.reanudar());
            tiempoReintento = INTERVALO_REINTENTO;
        }
    }

    boolean enviandoConfirmacion() {
        return pendiente != null && pendiente.reanudar();
    }

    private record Solicitud(String partidaId, int revision, boolean reanudar) {
    }
}
