package com.cartoonsurvivors.game.red;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;

public class ClienteUDP {

    private static final int PUERTO_SERVIDOR = 7777;
    private static final int TIEMPO_ESPERA_SOCKET = 500;
    private static final long INTERVALO_BUSQUEDA = 3000;

    private DatagramSocket socket;
    private InetAddress direccionServidor;
    private Thread hiloReceptor;
    private final EstadoConexion estadoConexion = new EstadoConexion();
    private final GestorEnemigos gestorEnemigos = new GestorEnemigos();
    private final GestorConexion gestorConexion = new GestorConexion(this);
    private boolean ejecutando = false;
    private String ultimoEstadoRecibido;
    private long ultimaBusqueda = 0;


    public synchronized void conectar() {

        if (ejecutando) {
            return;
        }

        guardarEstado(null);
        gestorEnemigos.reiniciar();

        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(TIEMPO_ESPERA_SOCKET);

            ejecutando = true;

            hiloReceptor = new HiloReceptor();
            hiloReceptor.start();

            buscarServidor();

        } catch (IOException e) {
            ejecutando = false;
            gestorConexion.reiniciar();
            estadoConexion.setSesionFinalizada(false);
            estadoConexion.setPartidaFinalizada(false);
            estadoConexion.setPartidaIniciada(false);
            estadoConexion.setConectado(false);
            estadoConexion.setServidorLleno(false);
            direccionServidor = null;
            estadoConexion.setIdJugador(0);

            if (socket != null) {
                socket.close();
                socket = null;
            }

            Gdx.app.error("ClienteUDP", "Error al iniciar cliente", e);
        }
    }

    private void buscarServidor() {

        if (!ejecutando || socket == null || socket.isClosed()) {
            return;
        }

        try {
            byte[] datos = ProtocoloRed.BUSCAR_SERVIDOR.getBytes(StandardCharsets.UTF_8);

            InetAddress broadcast = InetAddress.getByName("255.255.255.255");

            DatagramPacket paquete = new DatagramPacket(datos, datos.length, broadcast, PUERTO_SERVIDOR);

            socket.send(paquete);

            Gdx.app.log("ClienteUDP", "Buscando servidor en la red local...");

        } catch (IOException e) {
            Gdx.app.error("ClienteUDP", "Error buscando servidor", e);
        }
    }

    private class HiloReceptor extends Thread {

        @Override
        public void run() {
            escucharMensajes();
        }
    }

    private void escucharMensajes() {

        byte[] buffer = new byte[1024];

        while (ejecutando && socket != null && !socket.isClosed()) {

            try {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

                socket.receive(paquete);

                String mensaje = new String(paquete.getData(), paquete.getOffset(), paquete.getLength(), StandardCharsets.UTF_8).trim();

                if (paquete.getPort() != PUERTO_SERVIDOR) {
                    continue;
                }

                procesarMensaje(mensaje, paquete.getAddress());

            } catch (SocketTimeoutException e) {
                // Es normal: permite revisar periódicamente si seguimos ejecutando.

            } catch (SocketException e) {

                if (ejecutando) {
                    Gdx.app.error("ClienteUDP", "Error del socket", e);
                }

                break;

            } catch (IOException e) {

                if (ejecutando) {
                    Gdx.app.error("ClienteUDP", "Error recibiendo mensaje", e);
                }
            }
        }
    }


    private void procesarMensaje(String mensaje, InetAddress ipOrigen) {

        if (mensaje.equals(ProtocoloRed.SERVIDOR_ENCONTRADO)) {

            if (direccionServidor != null || estaServidorLleno()) {
                return;
            }

            direccionServidor = ipOrigen;

            Gdx.app.log("ClienteUDP", "Servidor encontrado: " + direccionServidor.getHostAddress());

            enviarMensaje(ProtocoloRed.CONECTAR);

            return;
        }

        if (mensaje.equals(ProtocoloRed.SERVIDOR_LLENO) || mensaje.equals("LLENO")) {

            estadoConexion.setServidorLleno(true);

            Gdx.app.log("ClienteUDP", "El servidor esta lleno");

            return;
        }

        if (direccionServidor == null || !direccionServidor.equals(ipOrigen)) {
            return;
        }

        if (mensaje.startsWith(ProtocoloRed.ESTADO + "|")) {

            guardarEstado(mensaje);

            return;
        }

        if (mensaje.startsWith(ProtocoloRed.ENEMIGOS + "|")) {

            gestorEnemigos.recibirBloque(mensaje);

            return;
        }

        if (mensaje.equals(ProtocoloRed.KEEP_ALIVE_OK)) {

            gestorConexion.registrarRespuesta();
            return;
        }

        if (mensaje.equals(ProtocoloRed.OK_JUGADOR_1)) {

            estadoConexion.setIdJugador(1);
            estadoConexion.setConectado(true);

            gestorConexion.registrarRespuesta();
            gestorConexion.iniciar();


            Gdx.app.log("ClienteUDP", "Jugador 1 conectado");

        } else if (mensaje.equals(ProtocoloRed.OK_JUGADOR_2)) {

            estadoConexion.setIdJugador(2);
            estadoConexion.setConectado(true);

            gestorConexion.registrarRespuesta();
            gestorConexion.iniciar();

            Gdx.app.log("ClienteUDP", "Jugador 2 conectado");

        } else if (mensaje.equals(ProtocoloRed.JUGADOR_CONECTADO)) {

            Gdx.app.log("ClienteUDP", "El otro jugador se conecto");

        } else if (mensaje.equals(ProtocoloRed.EMPIEZA)) {

            estadoConexion.setPartidaIniciada(true);
            estadoConexion.setPartidaFinalizada(false);

            Gdx.app.log("ClienteUDP", "Empieza la partida");

        } else if (mensaje.equals(ProtocoloRed.PARTIDA_FINALIZADA)) {

            estadoConexion.setPartidaIniciada(false);
            estadoConexion.setPartidaFinalizada(true);

            Gdx.app.log("ClienteUDP", "La partida finalizo");

        } else if (mensaje.equals(ProtocoloRed.SESION_FINALIZADA) || mensaje.startsWith(ProtocoloRed.SESION_FINALIZADA + "|")) {

            procesarSesionFinalizada(mensaje);

        } else if (mensaje.equals(ProtocoloRed.DESCONEXION_INESPERADA) || mensaje.equals("OTRO_JUGADOR_DESCONECTADO")) {

            procesarDesconexionInesperada();

        } else if (mensaje.startsWith(ProtocoloRed.JUGADOR_LISTO)) {

            Gdx.app.log("ClienteUDP", "Un jugador esta listo: " + mensaje);

        } else {

            Gdx.app.log("ClienteUDP", "Mensaje recibido: " + mensaje);
        }
    }


    public void enviarMovimiento(float direccionX, float direccionY) {

        if (!estaConectado() || !estaPartidaIniciada()) {
            return;
        }

        String mensaje = ProtocoloRed.MOVER + "|" + direccionX + "|" + direccionY;

        enviarMensaje(mensaje);
    }


    private void procesarSesionFinalizada(String mensaje) {

        estadoConexion.setSesionFinalizada(true);
        estadoConexion.setPartidaIniciada(false);
        estadoConexion.setConectado(false);

        gestorConexion.detener();

        Gdx.app.log("ClienteUDP", "La sesión finalizó. Motivo: " + mensaje);
    }



    private synchronized void guardarEstado(String mensaje) {

        ultimoEstadoRecibido = mensaje;
    }

    public synchronized String consumirUltimoEstado() {

        String mensaje = ultimoEstadoRecibido;

        ultimoEstadoRecibido = null;

        return mensaje;
    }

    private void procesarDesconexionInesperada() {

        estadoConexion.setSesionFinalizada(true);
        estadoConexion.setPartidaIniciada(false);
        estadoConexion.setConectado(false);

        gestorConexion.detener();

        Gdx.app.log("ClienteUDP", "El otro jugador se desconectó inesperadamente");
    }

    public synchronized void enviarMensaje(String mensaje) {

        if (!ejecutando || socket == null || socket.isClosed() || direccionServidor == null) {
            return;
        }

        try {
            byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);

            DatagramPacket paquete = new DatagramPacket(datos, datos.length, direccionServidor, PUERTO_SERVIDOR);

            socket.send(paquete);

        } catch (IOException e) {

            if (ejecutando) {
                Gdx.app.error("ClienteUDP", "Error enviando mensaje: " + mensaje, e);
            }
        }
    }


    public void enviarSeleccionPersonaje(TipoPersonaje personaje) {

        if (!estaConectado() || personaje == null) {
            return;
        }

        String mensaje = ProtocoloRed.SELECCIONAR_PERSONAJE + "|" + personaje.name();

        enviarMensaje(mensaje);

        Gdx.app.log("ClienteUDP", "Seleccion enviada: " + mensaje);
    }

    public synchronized void notificarServidorDesconectado() {

        if (!ejecutando || !estaConectado()) {
            return;
        }

        estadoConexion.setConectado(false);
        estadoConexion.setPartidaIniciada(false);
        estadoConexion.setPartidaFinalizada(false);
        estadoConexion.setSesionFinalizada(true);

        Gdx.app.log("ClienteUDP",
            "Se perdió la conexión con el servidor");

        gestorConexion.detener();
    }

    public String consumirEstadoEnemigos() {
        return gestorEnemigos.consumirEstadoEnemigos();
    }



    public void actualizar() {

        if (!ejecutando || estaConectado() || estaServidorLleno()) {
            return;
        }

        if (direccionServidor == null) {

            long tiempoActual = System.currentTimeMillis();

            if (tiempoActual - ultimaBusqueda >= INTERVALO_BUSQUEDA) {

                buscarServidor();

                ultimaBusqueda = tiempoActual;
            }
        }
    }


    /**
     * Se llama cuando el jugador presiona "Volver al menú".
     * Envía un único aviso al servidor y cierra el socket local.
     */
    public synchronized void volverAlMenu() {

        if (ejecutando && estaConectado()) {
            enviarMensaje(ProtocoloRed.VOLVER_AL_MENU);
        }

        cerrarConexionLocal();
    }

    /**
     * Cierra el cliente sin enviar otro mensaje al servidor.
     * Usar cuando el servidor ya notificó que la sesión terminó.
     */


    public synchronized void cerrarConexionLocal() {

        gestorConexion.reiniciar();
        guardarEstado(null);
        gestorEnemigos.reiniciar();
        ejecutando = false;
        estadoConexion.reiniciar();
        ultimaBusqueda = 0;
        direccionServidor = null;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
        Gdx.app.log("ClienteUDP", "Conexión local cerrada");
    }



    /**
     * Mantiene compatibilidad con llamadas anteriores a desconectar().
     * Ahora equivale a volver al menú.
     */
    public void desconectar() {
        volverAlMenu();
    }

    public boolean estaConectado() {
        return estadoConexion.estaConectado();
    }

    public boolean estaPartidaIniciada() {
        return estadoConexion.estaPartidaIniciada();
    }

    public boolean estaPartidaFinalizada() {
        return estadoConexion.estaPartidaFinalizada();
    }

    public boolean estaSesionFinalizada() {
        return estadoConexion.estaSesionFinalizada();
    }

    public boolean estaServidorLleno() {
        return estadoConexion.estaServidorLleno();
    }

    public int getIdJugador() {
        return estadoConexion.getIdJugador();
    }

    public boolean estaServidorDesconectado() {
        return gestorConexion.estaServidorDesconectado();
    }

    public String getIpServidor() {

        if (direccionServidor == null) {
            return "";
        }

        return direccionServidor.getHostAddress();
    }
}
