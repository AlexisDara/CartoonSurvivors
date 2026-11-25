package com.cartoonsurvivors.game.pantallas;

import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;

final class ControlPausa {
    private static final float INTERVALO_REINTENTO = 0.15f;

    private Solicitud pendiente;
    private float tiempoReintento;

    void solicitar(EstadisticasPartida estado, boolean reanudar) {
        if (estado == null || estado.getPausa() == null || estado.getPausa().activa() != reanudar) {
            return;
        }
        pendiente = new Solicitud(estado.getPartidaId(), estado.getPausa().getRevision(), reanudar);
        tiempoReintento = 0f;
    }

    void actualizar(float delta, EstadisticasPartida estado, int idLocal, ClienteUDP cliente) {
        if (pendiente == null) {
            return;
        }
        if (estado == null || estado.getPausa() == null || !pendiente.getPartidaId().equals(estado.getPartidaId())
            || pendiente.getRevision() != estado.getPausa().getRevision()
            || pendiente.getReanudar() != estado.getPausa().activa() || estado.getPausa().confirmado(idLocal)) {
            pendiente = null;
            return;
        }

        tiempoReintento -= delta;
        if (tiempoReintento <= 0f) {
            cliente.enviarPausa(pendiente.getPartidaId(), pendiente.getRevision(), pendiente.getReanudar());
            tiempoReintento = INTERVALO_REINTENTO;
        }
    }

    boolean enviandoConfirmacion() {
        return pendiente != null && pendiente.getReanudar();
    }

    private static final class Solicitud {
        private final String partidaId;
        private final int revision;
        private final boolean reanudar;

        Solicitud(String partidaId, int revision, boolean reanudar) {
            this.partidaId = partidaId;
            this.revision = revision;
            this.reanudar = reanudar;
        }

        public String getPartidaId() {
            return partidaId;
        }

        public int getRevision() {
            return revision;
        }

        public boolean getReanudar() {
            return reanudar;
        }
    }
}
