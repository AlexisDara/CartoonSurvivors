
package com.cartoonsurvivors.game.red;

import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.utilidades.TipoMapa;

public class EstadoConexion {

    private volatile boolean conectado;
    private volatile boolean partidaIniciada;
    private volatile boolean partidaFinalizada;
    private volatile boolean sesionFinalizada;
    private volatile boolean servidorLleno;

    private volatile TipoMapa mapaLobby = TipoMapa.MAPA1;
    private volatile TipoMapa mapaPartida = TipoMapa.MAPA1;
    private volatile int idJugador;

    public TipoMapa getMapaLobby() {
        return mapaLobby;
    }

    public void setMapaLobby(TipoMapa mapaLobby) {
        this.mapaLobby = mapaLobby;
    }

    public TipoMapa getMapaPartida() {
        return mapaPartida;
    }

    public void iniciarPartida(TipoMapa mapa) {
        mapaPartida = mapa;
        partidaIniciada = true;
    }

    private volatile TipoPersonaje personajeRemoto = TipoPersonaje.MORDECAI;

    public TipoPersonaje getPersonajeRemoto() {
        return personajeRemoto;
    }

    public void setPersonajeRemoto(TipoPersonaje personaje) {
        personajeRemoto = personaje;
    }

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
        mapaLobby = TipoMapa.MAPA1;
        mapaPartida = TipoMapa.MAPA1;
        personajeRemoto = TipoPersonaje.MORDECAI;
    }
}
