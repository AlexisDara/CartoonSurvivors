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
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;

public class ClienteUDP {

    private static final int PUERTO_SERVIDOR = 7777;
    private static final int TIEMPO_ESPERA_SOCKET = 500;
    private static final long INTERVALO_BUSQUEDA = 3000;

    private DatagramSocket socket;
    private InetAddress direccionServidor;

    private Thread hiloReceptor;
    private ScheduledExecutorService hiloKeepAlive;

    private boolean ejecutando = false;
    private boolean conectado = false;
    private boolean partidaIniciada = false;
    private boolean partidaFinalizada = false;
    private boolean sesionFinalizada = false;
    private boolean servidorLleno = false;
    private static final long TIEMPO_MAXIMO_SERVIDOR = 8000;
    private long ultimaRespuestaServidor = 0;
    private boolean servidorDesconectado = false;
    private String ultimoEstadoRecibido;

    private int idJugador = 0;

    private long ultimaBusqueda = 0;

    public synchronized void conectar() {

        if (ejecutando) {
            return;
        }

        guardarEstado(null);

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
            servidorDesconectado = false;
            sesionFinalizada = false;
            partidaFinalizada = false;
            partidaIniciada = false;
            conectado = false;
            servidorLleno = false;

            ultimaRespuestaServidor = 0;
            direccionServidor = null;
            idJugador = 0;

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

            if (direccionServidor != null || servidorLleno) {
                return;
            }

            direccionServidor = ipOrigen;

            Gdx.app.log("ClienteUDP", "Servidor encontrado: " + direccionServidor.getHostAddress());

            enviarMensaje(ProtocoloRed.CONECTAR);

            return;
        }

        if (mensaje.equals(ProtocoloRed.SERVIDOR_LLENO) || mensaje.equals("LLENO")) {

            servidorLleno = true;

            Gdx.app.log("ClienteUDP", "El servidor está lleno");

            return;
        }

        if (direccionServidor == null || !direccionServidor.equals(ipOrigen)) {
            return;
        }

        if (mensaje.equals(ProtocoloRed.KEEP_ALIVE_OK)) {

            registrarRespuestaServidor();

            return;
        }

        if (mensaje.equals(ProtocoloRed.OK_JUGADOR_1)) {

            idJugador = 1;
            conectado = true;
            registrarRespuestaServidor();

            iniciarKeepAlive();

            Gdx.app.log("ClienteUDP", "Jugador 1 conectado");

        } else if (mensaje.equals(ProtocoloRed.OK_JUGADOR_2)) {

            idJugador = 2;
            conectado = true;
            registrarRespuestaServidor();

            iniciarKeepAlive();

            Gdx.app.log("ClienteUDP", "Jugador 2 conectado");

        } else if (mensaje.equals(ProtocoloRed.JUGADOR_CONECTADO)) {

            Gdx.app.log("ClienteUDP", "El otro jugador se conectó");

        } else if (mensaje.equals(ProtocoloRed.EMPIEZA)) {

            partidaIniciada = true;
            partidaFinalizada = false;

            Gdx.app.log("ClienteUDP", "Empieza la partida");

        } else if (mensaje.equals(ProtocoloRed.PARTIDA_FINALIZADA)) {

            partidaIniciada = false;
            partidaFinalizada = true;

            Gdx.app.log("ClienteUDP", "La partida finalizó");

        } else if (mensaje.equals(ProtocoloRed.SESION_FINALIZADA) || mensaje.startsWith(ProtocoloRed.SESION_FINALIZADA + "|")) {

            procesarSesionFinalizada(mensaje);

        } else if (mensaje.equals(ProtocoloRed.DESCONEXION_INESPERADA) || mensaje.equals("OTRO_JUGADOR_DESCONECTADO")) {

            procesarDesconexionInesperada();

        } else if (mensaje.startsWith(ProtocoloRed.JUGADOR_LISTO)) {

            Gdx.app.log("ClienteUDP", "Un jugador está listo: " + mensaje);

        } else {

            Gdx.app.log("ClienteUDP", "Mensaje recibido: " + mensaje);
        }
    }


    public void enviarMovimiento(
        float direccionX,
        float direccionY) {

        if (!estaConectado() || !estaPartidaIniciada()) {
            return;
        }

        String mensaje =
            ProtocoloRed.MOVER
                + "|" + direccionX
                + "|" + direccionY;

        enviarMensaje(mensaje);
    }


    private void procesarSesionFinalizada(String mensaje) {

        sesionFinalizada = true;
        partidaIniciada = false;
        conectado = false;

        detenerKeepAlive();

        Gdx.app.log("ClienteUDP", "La sesión finalizó. Motivo: " + mensaje);
    }


    private synchronized void comprobarServidor() {

        if (!ejecutando || !conectado || servidorDesconectado) {
            return;
        }

        long tiempoActual = System.currentTimeMillis();

        long tiempoSinRespuesta =
            tiempoActual - ultimaRespuestaServidor;

        if (tiempoSinRespuesta > TIEMPO_MAXIMO_SERVIDOR) {

            servidorDesconectado = true;

            conectado = false;
            partidaIniciada = false;
            partidaFinalizada = false;
            sesionFinalizada = true;

            Gdx.app.log(
                "ClienteUDP",
                "Se perdió la conexión con el servidor"
            );

            detenerKeepAlive();
        }
    }


    private synchronized void guardarEstado(String mensaje) {

        ultimoEstadoRecibido = mensaje;
    }

    public synchronized String consumirUltimoEstado() {

        String mensaje = ultimoEstadoRecibido;

        ultimoEstadoRecibido = null;

        return mensaje;
    }



    private synchronized void registrarRespuestaServidor() {

        ultimaRespuestaServidor =
            System.currentTimeMillis();
    }

    private void procesarDesconexionInesperada() {

        sesionFinalizada = true;
        partidaIniciada = false;
        conectado = false;

        detenerKeepAlive();

        Gdx.app.log("ClienteUDP", "El otro jugador se desconectó inesperadamente");
    }

    private synchronized void iniciarKeepAlive() {

        if (hiloKeepAlive != null && !hiloKeepAlive.isShutdown()) {
            return;
        }

        hiloKeepAlive = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread hilo = new Thread(runnable, "ClienteUDP-KeepAlive");
            hilo.setDaemon(true);
            return hilo;
        });

        hiloKeepAlive.scheduleAtFixedRate(() -> {

            comprobarServidor();

            if (ejecutando && conectado) {

                enviarMensaje(ProtocoloRed.KEEP_ALIVE);
            }

        }, 0, 2, TimeUnit.SECONDS);

        Gdx.app.log("ClienteUDP", "KeepAlive iniciado");
    }

    private synchronized void detenerKeepAlive() {

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

            if (ejecutando) {
                Gdx.app.error("ClienteUDP", "Error enviando mensaje: " + mensaje, e);
            }
        }
    }


    public void enviarSeleccionPersonaje(TipoPersonaje personaje) {

        if (!conectado || personaje == null) {
            return;
        }

        String mensaje =
            ProtocoloRed.SELECCIONAR_PERSONAJE
                + "|" + personaje.name();

        enviarMensaje(mensaje);

        Gdx.app.log(
            "ClienteUDP",
            "Seleccion enviada: " + mensaje
        );
    }


    public void actualizar() {

        if (!ejecutando || conectado || servidorLleno) {
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

        if (ejecutando && conectado) {
            enviarMensaje(ProtocoloRed.VOLVER_AL_MENU);
        }

        cerrarConexionLocal();
    }

    /**
     * Cierra el cliente sin enviar otro mensaje al servidor.
     * Usar cuando el servidor ya notificó que la sesión terminó.
     */
    public synchronized void cerrarConexionLocal() {

        detenerKeepAlive();
        guardarEstado(null);
        ejecutando = false;
        conectado = false;
        partidaIniciada = false;
        ultimaRespuestaServidor = 0;
        servidorDesconectado = false;
        sesionFinalizada = false;
        partidaFinalizada = false;
        servidorLleno = false;

        idJugador = 0;
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
        return conectado;
    }

    public boolean estaPartidaIniciada() {
        return partidaIniciada;
    }

    public boolean estaPartidaFinalizada() {
        return partidaFinalizada;
    }

    public boolean estaSesionFinalizada() {
        return sesionFinalizada;
    }

    public boolean estaServidorLleno() {
        return servidorLleno;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public synchronized boolean estaServidorDesconectado() {
        return servidorDesconectado;
    }

    public String getIpServidor() {

        if (direccionServidor == null) {
            return "";
        }

        return direccionServidor.getHostAddress();
    }
}
