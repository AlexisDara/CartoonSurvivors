package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.red.ProtocoloRed;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class SincronizadorJugadores {

    private final Jugador jugadorLocal;
    private Jugador jugadorRemoto;
    private final int idJugadorLocal;
    private final Map<Jugador, Map<Integer, Long>> ultimosAtaques = new IdentityHashMap<>();
    private long ultimaSecuencia = -1;
    private boolean estadoRecibido = false;
    private EstadisticasPartida estadisticasPartida;
    private long ultimaSecuenciaHud = -1;

    public SincronizadorJugadores(Jugador jugadorLocal, Jugador jugadorRemoto, int idJugadorLocal) {

        this.jugadorLocal = jugadorLocal;
        this.jugadorRemoto = jugadorRemoto;
        this.idJugadorLocal = idJugadorLocal;
    }

    public void cambiarJugadorRemoto(Jugador nuevoJugador) {
        Map<Integer, Long> ataques = ultimosAtaques.remove(jugadorRemoto);
        jugadorRemoto = nuevoJugador;
        if (ataques != null) {
            ultimosAtaques.put(jugadorRemoto, ataques);
        }
    }

    public void procesarEstado(String mensaje) {

        if (mensaje == null) {
            return;
        }

        // ESTADO|SECUENCIA|JUGADOR1|JUGADOR2
        String[] partes = mensaje.split("\\|", -1);

        if (partes.length != 4 || !partes[0].equals("ESTADO")) {
            return;
        }

        try {
            long secuencia = Long.parseLong(partes[1]);

            if (secuencia <= ultimaSecuencia) {
                return;
            }

            DatosJugador datos1 = interpretarJugador(partes[2]);
            DatosJugador datos2 = interpretarJugador(partes[3]);

            if (datos1 == null || datos2 == null) {
                return;
            }

            if (datos1.id() != 1 || datos2.id() != 2) {
                return;
            }

            if (idJugadorLocal == 1) {
                aplicarEstado(jugadorLocal, datos1);
                aplicarEstado(jugadorRemoto, datos2);
            } else {
                aplicarEstado(jugadorLocal, datos2);
                aplicarEstado(jugadorRemoto, datos1);
            }

            ultimaSecuencia = secuencia;
            estadoRecibido = true;

        } catch (NumberFormatException e) {
            Gdx.app.error("SincronizadorJugadores", "Estado invalido: " + mensaje);
        }
    }

    private DatosJugador interpretarJugador(String datos) {

        // ID:X:Y:DX:DY:VIDA:ARMAS
        String[] partes = datos.split(":", -1);

        if (partes.length != 7) {
            return null;
        }

        int id = Integer.parseInt(partes[0]);

        float x = Float.parseFloat(partes[1]);
        float y = Float.parseFloat(partes[2]);

        float direccionX = Float.parseFloat(partes[3]);
        float direccionY = Float.parseFloat(partes[4]);

        float vida = Float.parseFloat(partes[5]);

        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(direccionX) || !Float.isFinite(direccionY) || !Float.isFinite(vida) || vida < 0) {
            return null;
        }

        return new DatosJugador(id, x, y, direccionX, direccionY, vida, partes[6]);
    }

    private void aplicarEstado(Jugador jugador, DatosJugador datos) {

        jugador.setPosicion(datos.x(), datos.y());

        // La vida llega calculada desde el servidor.
        // El cliente solamente actualiza el valor visual.
        jugador.setVida(datos.vida());

        jugador.seMueve(datos.direccionX(), datos.direccionY());

        jugador.calcularLadoMirada(datos.direccionX());

        actualizarArmas(jugador, datos.armas());
    }

    private void actualizarArmas(Jugador jugador, String datosArmas) {

        if (datosArmas.isEmpty()) {
            return;
        }

        Map<Integer, Long> ataquesJugador = ultimosAtaques.computeIfAbsent(jugador, j -> new HashMap<>());

        String[] armasRecibidas = datosArmas.split(";");

        for (String datosArma : armasRecibidas) {

            // SLOT,TIPO,NUMERO_ATAQUE,DIRECCION
            String[] partes = datosArma.split(",", -1);

            if (partes.length != 4) {
                continue;
            }

            try {
                int slot = Integer.parseInt(partes[0]);
                String tipo = partes[1];

                long numeroAtaque = Long.parseLong(partes[2]);
                int direccion = Integer.parseInt(partes[3]);

                if (slot < 0 || numeroAtaque <= 0) {
                    continue;
                }

                Long ultimoAtaque = ataquesJugador.get(slot);

                if (ultimoAtaque != null && numeroAtaque <= ultimoAtaque) {
                    continue;
                }

                ataquesJugador.put(slot, numeroAtaque);

                if (tipo.equals("ESPADA") && slot == 0) {

                    Arma arma = jugador.getArma();

                    if (arma instanceof Espada espada) {
                        espada.reproducirAtaque(jugador.getHitbox(), direccion);
                    }
                }

            } catch (NumberFormatException e) {
                Gdx.app.error("SincronizadorJugadores", "Ataque invalido: " + datosArma);
            }
        }
    }

    public boolean hayEstadoRecibido() {
        return estadoRecibido;
    }

    public void procesarEstadoHud(String mensaje) {
        if (mensaje == null) {
            return;
        }

        // HUD|SECUENCIA|SEGUNDOS|KILLS
        String[] partes = mensaje.split("\\|", -1);
        if (partes.length != 4 || !ProtocoloRed.HUD.equals(partes[0])) {
            return;
        }

        try {
            long secuencia = Long.parseLong(partes[1]);
            int segundos = Integer.parseInt(partes[2]);
            int kills = Integer.parseInt(partes[3]);
            if (secuencia > ultimaSecuenciaHud && segundos >= 0 && kills >= 0) {
                ultimaSecuenciaHud = secuencia;
                estadisticasPartida = new EstadisticasPartida(segundos, kills);
            }
        } catch (NumberFormatException e) {
            Gdx.app.error("SincronizadorJugadores", "Estado HUD invalido: " + mensaje);
        }
    }

    public EstadisticasPartida getEstadisticasPartida() {
        return estadisticasPartida;
    }

    public record EstadisticasPartida(int segundos, int kills) {
    }

    private record DatosJugador(int id, float x, float y, float direccionX, float direccionY, float vida,
                                String armas) {
    }
}
