package com.cartoonsurvivors.game.red;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GestorConexion {

    private static final long TIEMPO_MAXIMO_SERVIDOR = 8000;

    private ScheduledExecutorService hiloKeepAlive;

    private long ultimaRespuestaServidor;
    private boolean servidorDesconectado;

    private final ClienteUDP clienteUDP;

    public GestorConexion(ClienteUDP clienteUDP) {
        this.clienteUDP = clienteUDP;
    }

    public synchronized void iniciar() {

        if (hiloKeepAlive != null && !hiloKeepAlive.isShutdown()) {
            return;
        }

        hiloKeepAlive = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread hilo = new Thread(runnable, "ClienteUDP-KeepAlive");
            hilo.setDaemon(true);
            return hilo;
        });

        hiloKeepAlive.scheduleAtFixedRate(() -> {

            if (comprobarDesconexion()) {
                clienteUDP.notificarServidorDesconectado();
                return;
            }

            if (clienteUDP.estaConectado()) {
                clienteUDP.enviarMensaje(ProtocoloRed.KEEP_ALIVE);
            }

        }, 0, 2, TimeUnit.SECONDS);
    }

    public synchronized void registrarRespuesta() {
        ultimaRespuestaServidor = System.currentTimeMillis();
    }

    private synchronized boolean comprobarDesconexion() {

        if (!clienteUDP.estaConectado() || servidorDesconectado) {
            return false;
        }

        long tiempoActual = System.currentTimeMillis();

        if (tiempoActual - ultimaRespuestaServidor > TIEMPO_MAXIMO_SERVIDOR) {
            servidorDesconectado = true;
            return true;
        }

        return false;
    }

    public synchronized void detener() {

        if (hiloKeepAlive != null) {
            hiloKeepAlive.shutdownNow();
            hiloKeepAlive = null;
        }
    }

    public synchronized void reiniciar() {

        detener();

        ultimaRespuestaServidor = 0;
        servidorDesconectado = false;
    }

    public synchronized boolean estaServidorDesconectado() {
        return servidorDesconectado;
    }
}
