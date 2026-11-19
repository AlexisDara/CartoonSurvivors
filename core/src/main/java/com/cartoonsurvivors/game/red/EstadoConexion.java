
package com.cartoonsurvivors.game.red;

public class EstadoConexion {

    private volatile boolean conectado;
    private volatile boolean partidaIniciada;
    private volatile boolean partidaFinalizada;
    private volatile boolean sesionFinalizada;
    private volatile boolean servidorLleno;

    private volatile int idJugador;

    public boolean estaConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }

    public boolean estaPartidaIniciada() {
        return partidaIniciada;
    }

    public void setPartidaIniciada(boolean partidaIniciada) {
        this.partidaIniciada = partidaIniciada;
    }

    public boolean estaPartidaFinalizada() {
        return partidaFinalizada;
    }

    public void setPartidaFinalizada(boolean partidaFinalizada) {
        this.partidaFinalizada = partidaFinalizada;
    }

    public boolean estaSesionFinalizada() {
        return sesionFinalizada;
    }

    public void setSesionFinalizada(boolean sesionFinalizada) {
        this.sesionFinalizada = sesionFinalizada;
    }

    public boolean estaServidorLleno() {
        return servidorLleno;
    }

    public void setServidorLleno(boolean servidorLleno) {
        this.servidorLleno = servidorLleno;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }

    public void reiniciar() {
        conectado = false;
        partidaIniciada = false;
        partidaFinalizada = false;
        sesionFinalizada = false;
        servidorLleno = false;
        idJugador = 0;
    }
}
