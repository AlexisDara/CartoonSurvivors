package com.cartoonsurvivors.game.red;

import com.badlogic.gdx.Gdx;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ClienteUDP {

    private static final int PUERTO_SERVIDOR = 7777;
    private static final int TIEMPO_ESPERA_SOCKET = 500;
    private static final long INTERVALO_BUSQUEDA = 3000;

    private volatile DatagramSocket socket;
    private volatile InetAddress direccionServidor;
    private volatile boolean ejecutando;

    private Thread hiloReceptor;
    private long ultimaBusqueda;

    private String ultimoEstadoRecibido;
    private String ultimoEstadoHud;

    private final EstadoConexion estadoConexion = new EstadoConexion();

    private final GestorEnemigos gestorEnemigos = new GestorEnemigos();

    private final GestorConexion gestorConexion = new GestorConexion(this);

    private final ProcesadorMensajes procesadorMensajes = new ProcesadorMensajes(estadoConexion, gestorConexion, gestorEnemigos, this::registrarServidor, () -> direccionServidor, this::guardarEstado, this::guardarEstadoHud);

    public synchronized void conectar() {

        if (ejecutando) {
            return;
        }

        estadoConexion.reiniciar();
        gestorConexion.reiniciar();
        gestorEnemigos.reiniciar();
        guardarEstado(null);
        guardarEstadoHud(null);

        direccionServidor = null;
        ultimaBusqueda = 0;

        try {
            DatagramSocket nuevoSocket = new DatagramSocket();

            try {
                nuevoSocket.setBroadcast(true);
                nuevoSocket.setSoTimeout(TIEMPO_ESPERA_SOCKET);
            } catch (IOException e) {
                nuevoSocket.close();
                throw e;
            }

            socket = nuevoSocket;
            ejecutando = true;

            hiloReceptor = new Thread(() -> escucharMensajes(nuevoSocket), "ClienteUDP-Receptor");

            hiloReceptor.setDaemon(true);
            hiloReceptor.start();

            buscarServidor();

        } catch (IOException e) {

            ejecutando = false;
            estadoConexion.reiniciar();
            gestorConexion.reiniciar();

            DatagramSocket actual = socket;
            socket = null;
            direccionServidor = null;

            if (actual != null) {
                actual.close();
            }

            Gdx.app.error("ClienteUDP", "Error al iniciar cliente", e);
        }
    }

    private void buscarServidor() {

        DatagramSocket actual = socket;

        if (!ejecutando || actual == null || actual.isClosed()) {
            return;
        }

        try {
            byte[] datos = ProtocoloRed.BUSCAR_SERVIDOR.getBytes(StandardCharsets.UTF_8);

            InetAddress broadcast = InetAddress.getByName("255.255.255.255");

            DatagramPacket paquete = new DatagramPacket(datos, datos.length, broadcast, PUERTO_SERVIDOR);

            actual.send(paquete);

            Gdx.app.log("ClienteUDP", "Buscando servidor en la red local...");

        } catch (IOException e) {
            if (ejecutando) {
                Gdx.app.error("ClienteUDP", "Error buscando servidor", e);
            }
        }
    }

    private void escucharMensajes(DatagramSocket socketActivo) {

        byte[] buffer = new byte[1024];

        while (ejecutando && !socketActivo.isClosed()) {

            try {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

                socketActivo.receive(paquete);

                // Ignorar paquetes de una conexion anterior.
                if (!ejecutando || socket != socketActivo) {
                    break;
                }

                if (paquete.getPort() != PUERTO_SERVIDOR) {
                    continue;
                }

                String mensaje = new String(paquete.getData(), paquete.getOffset(), paquete.getLength(), StandardCharsets.UTF_8).trim();

                procesadorMensajes.procesar(mensaje, paquete.getAddress());

            } catch (SocketTimeoutException e) {
                // Timeout normal de recepcion.

            } catch (SocketException e) {

                if (ejecutando && socket == socketActivo) {
                    Gdx.app.error("ClienteUDP", "Error del socket", e);
                }

                break;

            } catch (IOException e) {

                if (ejecutando && socket == socketActivo) {
                    Gdx.app.error("ClienteUDP", "Error recibiendo mensaje", e);
                }
            }
        }
    }

    private synchronized void registrarServidor(InetAddress ipServidor) {

        if (!ejecutando || direccionServidor != null || estaServidorLleno()) {
            return;
        }

        direccionServidor = ipServidor;

        Gdx.app.log("ClienteUDP", "Servidor encontrado: " + ipServidor.getHostAddress());

        enviarMensaje(ProtocoloRed.CONECTAR);
    }

    public synchronized void enviarMensaje(String mensaje) {

        DatagramSocket actual = socket;
        InetAddress servidor = direccionServidor;

        if (!ejecutando || actual == null || actual.isClosed() || servidor == null) {
            return;
        }

        try {
            byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);

            DatagramPacket paquete = new DatagramPacket(datos, datos.length, servidor, PUERTO_SERVIDOR);

            actual.send(paquete);

        } catch (IOException e) {

            if (ejecutando) {
                Gdx.app.error("ClienteUDP", "Error enviando mensaje: " + mensaje, e);
            }
        }
    }

    public void enviarMovimiento(float direccionX, float direccionY) {

        if (!estaConectado() || !estaPartidaIniciada()) {
            return;
        }

        String mensaje = ProtocoloRed.MOVER + "|" + direccionX + "|" + direccionY;

        enviarMensaje(mensaje);
    }

    public void enviarMejora(String partidaId, int nivel, int revision, int opcion) {
        if (estaConectado() && estaPartidaIniciada()) {
            enviarMensaje(ProtocoloRed.MEJORA + "|" + partidaId + "|" + nivel + "|" + revision + "|" + opcion);
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

    private synchronized void guardarEstado(String mensaje) {
        ultimoEstadoRecibido = mensaje;
    }

    private synchronized void guardarEstadoHud(String mensaje) {
        ultimoEstadoHud = mensaje;
    }

    public synchronized String consumirEstadoHud() {
        String mensaje = ultimoEstadoHud;
        ultimoEstadoHud = null;
        return mensaje;
    }

    public synchronized String consumirUltimoEstado() {

        String mensaje = ultimoEstadoRecibido;
        ultimoEstadoRecibido = null;

        return mensaje;
    }

    public String consumirEstadoEnemigos() {
        return gestorEnemigos.consumirEstadoEnemigos();
    }

    public synchronized void notificarServidorDesconectado() {

        if (!ejecutando || !estaConectado()) {
            return;
        }

        estadoConexion.setConectado(false);
        estadoConexion.setPartidaIniciada(false);
        estadoConexion.setPartidaFinalizada(false);
        estadoConexion.setSesionFinalizada(true);

        Gdx.app.log("ClienteUDP", "Se perdio la conexion con el servidor");

        gestorConexion.detener();
    }

    public void actualizar() {

        if (!ejecutando || estaConectado() || estaServidorLleno()) {
            return;
        }

        if (direccionServidor == null) {

            long ahora = System.currentTimeMillis();

            if (ahora - ultimaBusqueda >= INTERVALO_BUSQUEDA) {

                buscarServidor();
                ultimaBusqueda = ahora;
            }
        }
    }

    public synchronized void volverAlMenu() {

        if (ejecutando && estaConectado()) {
            enviarMensaje(ProtocoloRed.VOLVER_AL_MENU);
        }

        cerrarConexionLocal();
    }

    public synchronized void cerrarConexionLocal() {

        // Marcar primero la conexion como inactiva.
        ejecutando = false;

        gestorConexion.reiniciar();
        estadoConexion.reiniciar();
        gestorEnemigos.reiniciar();

        guardarEstado(null);
        guardarEstadoHud(null);

        ultimaBusqueda = 0;
        direccionServidor = null;

        DatagramSocket actual = socket;
        socket = null;

        if (actual != null && !actual.isClosed()) {
            actual.close();
        }

        hiloReceptor = null;

        Gdx.app.log("ClienteUDP", "Conexion local cerrada");
    }

    public void desconectar() {
        volverAlMenu();
    }

    public boolean estaConectado() {
        return estadoConexion.estaConectado();
    }

    public boolean estaPartidaIniciada() {
        return estadoConexion.estaPartidaIniciada();
    }

    public TipoPersonaje getPersonajeRemoto() {
        return estadoConexion.getPersonajeRemoto();
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

        InetAddress servidor = direccionServidor;

        return servidor == null ? "" : servidor.getHostAddress();
    }
}
