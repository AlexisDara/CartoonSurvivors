package com.cartoonsurvivors.game.red;

import com.badlogic.gdx.Gdx;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.utilidades.TipoMapa;

import java.net.InetAddress;

public class ProcesadorMensajes {

    private final EstadoConexion estadoConexion;
    private final GestorConexion gestorConexion;
    private final GestorEnemigos gestorEnemigos;

    private final ClienteUDP cliente;

    public ProcesadorMensajes(ClienteUDP cliente, EstadoConexion estadoConexion, GestorConexion gestorConexion,
                              GestorEnemigos gestorEnemigos) {

        this.cliente = cliente;
        this.estadoConexion = estadoConexion;
        this.gestorConexion = gestorConexion;
        this.gestorEnemigos = gestorEnemigos;
    }

    public void procesar(String mensaje, InetAddress ipOrigen) {

        if (mensaje == null || ipOrigen == null) {
            return;
        }

        if (mensaje.equals(ProtocoloRed.SERVIDOR_ENCONTRADO)) {
            if (!estadoConexion.estaServidorLleno()) {
                cliente.registrarServidor(ipOrigen);
            }
            return;
        }

        if (mensaje.equals(ProtocoloRed.SERVIDOR_LLENO) || mensaje.equals("LLENO")) {

            InetAddress servidor = cliente.getDireccionServidor();

            if (servidor != null && !servidor.equals(ipOrigen)) {
                return;
            }

            estadoConexion.setServidorLleno(true);
            Gdx.app.log("ProcesadorMensajes", "El servidor esta lleno");
            return;
        }

        InetAddress servidor = cliente.getDireccionServidor();

        if (servidor == null || !servidor.equals(ipOrigen)) {
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.ESTADO + "|")) {
            cliente.guardarEstado(mensaje);
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.HUD + "|")) {
            cliente.guardarEstadoHud(mensaje);
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.ENEMIGOS + "|")) {
            gestorEnemigos.recibirBloque(mensaje);
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.PERSONAJES_ACTUALIZADOS + "|")) {
            actualizarPersonajeRemoto(mensaje);
            return;
        }

        if (mensaje.equals(ProtocoloRed.KEEP_ALIVE_OK)) {
            gestorConexion.registrarRespuesta();
            return;
        }

        if (mensaje.equals(ProtocoloRed.OK_JUGADOR_1)) {
            conectarJugador(1);
            return;
        }

        if (mensaje.equals(ProtocoloRed.OK_JUGADOR_2)) {
            conectarJugador(2);
            return;
        }

        if (mensaje.equals(ProtocoloRed.JUGADOR_CONECTADO)) {
            Gdx.app.log("ProcesadorMensajes", "El otro jugador se conecto");
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.MAPA + "|")) {
            TipoMapa mapa = TipoMapa.desdeId(mensaje.substring(ProtocoloRed.MAPA.length() + 1));
            if (mapa != null) {
                estadoConexion.setMapaLobby(mapa);
            }
            return;
        }

        if (mensaje.equals(ProtocoloRed.EMPIEZA) || mensaje.startsWith(ProtocoloRed.EMPIEZA + "|")) {
            TipoMapa mapa;
            if (mensaje.equals(ProtocoloRed.EMPIEZA)) {
                mapa = TipoMapa.MAPA1;
            } else {
                mapa = TipoMapa.desdeId(mensaje.substring(ProtocoloRed.EMPIEZA.length() + 1));
            }
            if (mapa != null && estadoConexion.estaConectado() && !estadoConexion.estaPartidaIniciada()
                    && !estadoConexion.estaPartidaFinalizada()) {
                estadoConexion.iniciarPartida(mapa);
                Gdx.app.log("ProcesadorMensajes", "Empieza la partida en " + mapa.getNombre());
            }
            return;
        }

        if (mensaje.equals(ProtocoloRed.PARTIDA_FINALIZADA)) {
            estadoConexion.setPartidaIniciada(false);
            estadoConexion.setPartidaFinalizada(true);

            Gdx.app.log("ProcesadorMensajes", "La partida finalizo");
            return;
        }

        if (mensaje.equals(ProtocoloRed.SESION_FINALIZADA) || mensaje.startsWith(ProtocoloRed.SESION_FINALIZADA + "|")) {

            finalizarSesion(mensaje);
            return;
        }

        if (mensaje.equals(ProtocoloRed.DESCONEXION_INESPERADA) || mensaje.equals("OTRO_JUGADOR_DESCONECTADO")) {

            finalizarSesion(mensaje);
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.JUGADOR_LISTO)) {
            Gdx.app.log("ProcesadorMensajes", "Un jugador esta listo: " + mensaje);
            return;
        }

        Gdx.app.log("ProcesadorMensajes", "Mensaje recibido: " + mensaje);
    }

    private void actualizarPersonajeRemoto(String mensaje) {
        int idLocal = estadoConexion.getIdJugador();
        if (idLocal != 1 && idLocal != 2) {
            return;
        }

        String prefijoRemoto = (3 - idLocal) + ":";
        String[] campos = mensaje.split("\\|");
        for (int i = 1; i < campos.length; i++) {
            if (campos[i].startsWith(prefijoRemoto)) {
                String nombre = campos[i].substring(prefijoRemoto.length());
                for (TipoPersonaje tipo : TipoPersonaje.values()) {
                    if (tipo.name().equals(nombre)) {
                        estadoConexion.setPersonajeRemoto(tipo);
                        return;
                    }
                }
            }
        }
    }

    private void conectarJugador(int idJugador) {

        estadoConexion.setIdJugador(idJugador);
        estadoConexion.setConectado(true);

        gestorConexion.registrarRespuesta();
        gestorConexion.iniciar();

        Gdx.app.log("ProcesadorMensajes", "Jugador " + idJugador + " conectado");
    }

    private void finalizarSesion(String motivo) {

        estadoConexion.setSesionFinalizada(true);
        estadoConexion.setPartidaIniciada(false);
        estadoConexion.setConectado(false);

        gestorConexion.detener();

        Gdx.app.log("ProcesadorMensajes", "La sesion finalizo. Motivo: " + motivo);
    }
}
