package com.cartoonsurvivors.game.red;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClienteUDP {

    private static final int PUERTO_SERVIDOR = 7777;

    private DatagramSocket socket;
    private InetAddress direccionServidor;

    private Thread hiloReceptor;
    private ScheduledExecutorService hiloKeepAlive;

    private volatile boolean ejecutando = false;
    private volatile boolean conectado = false;
    private volatile boolean partidaIniciada = false;
    private volatile boolean partidaFinalizada = false;

    private volatile int idJugador = 0;

    private long ultimaBusqueda = 0;

    public void conectar() {

        if (ejecutando) {
            return;
        }

        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(500);
            ejecutando = true;
            hiloReceptor = new HiloReceptor();
            hiloReceptor.start();
            buscarServidor();

        } catch (IOException e) {
            ejecutando = false;
            if (socket != null) {
                socket.close();
            }
            Gdx.app.error("ClienteUDP", "Error al iniciar cliente", e);
        }
    }

    private void buscarServidor() {

        try {

            byte[] datos = "BUSCAR_SERVIDOR".getBytes(StandardCharsets.UTF_8);

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

        while (ejecutando && !socket.isClosed()) {

            try {

                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

                socket.receive(paquete);

                String mensaje = new String(paquete.getData(), paquete.getOffset(), paquete.getLength(), StandardCharsets.UTF_8).trim();

                if (paquete.getPort() != PUERTO_SERVIDOR) {
                    continue;
                }

                procesarMensaje(mensaje, paquete.getAddress());

            } catch (SocketTimeoutException e) {


            } catch (SocketException e) {

                if (ejecutando) {
                    Gdx.app.error("ClienteUDP", "Error del socket", e);
                }

                break;

            } catch (IOException e) {

                Gdx.app.error("ClienteUDP", "Error recibiendo mensaje", e);
            }
        }
    }

    private void procesarMensaje(String mensaje, InetAddress ipOrigen) {

        if (mensaje.equals("SERVIDOR_ENCONTRADO")) {

            if (direccionServidor != null) {
                return;
            }

            direccionServidor = ipOrigen;

            Gdx.app.log("ClienteUDP", "Servidor encontrado: " + direccionServidor.getHostAddress());

            enviarMensaje("CONECTAR");

            return;
        }

        if (direccionServidor == null || !direccionServidor.equals(ipOrigen)) {
            return;
        }

        switch (mensaje) {

            case "OK-1":

                idJugador = 1;
                conectado = true;

                iniciarKeepAlive();

                Gdx.app.log("ClienteUDP", "Jugador 1 conectado");

                break;

            case "OK-2":

                idJugador = 2;
                conectado = true;

                iniciarKeepAlive();

                Gdx.app.log("ClienteUDP", "Jugador 2 conectado");

                break;

            case "EMPIEZA":

                partidaIniciada = true;

                Gdx.app.log("ClienteUDP", "Empieza la partida");

                break;

            case "LLENO":

                Gdx.app.log("ClienteUDP", "Servidor lleno");

                break;

            case "OTRO_JUGADOR_DESCONECTADO":

                partidaFinalizada = true;
                partidaIniciada = false;

                Gdx.app.log("ClienteUDP", "El otro jugador se desconectó");

                break;

            case "PARTIDA_FINALIZADA":

                Gdx.app.log("ClienteUDP", "La partida anterior ya finalizó");

                break;

            default:
                Gdx.app.log("ClienteUDP", "Mensaje recibido: " + mensaje);
                break;
        }
    }

    private void iniciarKeepAlive() {

        if (hiloKeepAlive != null && !hiloKeepAlive.isShutdown()) {
            return;
        }

        hiloKeepAlive = Executors.newSingleThreadScheduledExecutor();

        hiloKeepAlive.scheduleAtFixedRate(() -> enviarMensaje("KEEP_ALIVE"), 0, 2, TimeUnit.SECONDS);

        Gdx.app.log("ClienteUDP", "KeepAlive iniciado");
    }

    private void detenerKeepAlive() {

        if (hiloKeepAlive != null) {

            hiloKeepAlive.shutdownNow();
            hiloKeepAlive = null;
        }
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

            Gdx.app.error("ClienteUDP", "Error enviando mensaje", e);
        }
    }

    public void actualizar() {

        if (!ejecutando) {
            return;
        }

        if (direccionServidor == null) {

            long tiempoActual = System.currentTimeMillis();

            if (tiempoActual - ultimaBusqueda >= 3000) {

                buscarServidor();

                ultimaBusqueda = tiempoActual;
            }
        }
    }

    public void desconectar() {

        if (ejecutando) {
            enviarMensaje("DESCONECTAR");
        }

        detenerKeepAlive();

        ejecutando = false;
        conectado = false;
        partidaIniciada = false;
        partidaFinalizada = false;

        idJugador = 0;
        ultimaBusqueda = 0;

        direccionServidor = null;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        socket = null;

        Gdx.app.log("ClienteUDP", "Cliente desconectado");
    }

    public boolean estaConectado() {
        return conectado;
    }

    public boolean estaPartidaIniciada() {
        return partidaIniciada;
    }

    public boolean estaPartidaFinalizada() {
        return partidaFinalizada;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public String getIpServidor() {

        if (direccionServidor == null) {
            return "";
        }

        return direccionServidor.getHostAddress();
    }
}
