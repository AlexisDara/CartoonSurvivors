package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.armas.Pistola;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.red.ProtocoloRed;
import com.cartoonsurvivors.game.utilidades.Constantes;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
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
        ultimosAtaques.remove(jugadorRemoto);
        jugadorRemoto = nuevoJugador;
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

        // ID:X:Y:DX:DY:VIDA:ARMAS[:VIDA_MAXIMA[:SEGUNDOS_REAPARICION]]
        String[] partes = datos.split(":", -1);

        if (partes.length < 7 || partes.length > 9) {
            return null;
        }

        int id = Integer.parseInt(partes[0]);

        float x = Float.parseFloat(partes[1]);
        float y = Float.parseFloat(partes[2]);

        float direccionX = Float.parseFloat(partes[3]);
        float direccionY = Float.parseFloat(partes[4]);

        float vida = Float.parseFloat(partes[5]);
        float vidaMaxima = partes.length >= 8 ? Float.parseFloat(partes[7]) : Constantes.Jugador.VIDA_INICIAL;
        int segundosParaReaparecer = partes.length == 9 ? Integer.parseInt(partes[8]) : 0;

        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(direccionX) || !Float.isFinite(direccionY)
            || !Float.isFinite(vida) || !Float.isFinite(vidaMaxima) || vidaMaxima <= 0 || vida < 0 || vida > vidaMaxima
            || segundosParaReaparecer < 0) {
            return null;
        }

        return new DatosJugador(id, x, y, direccionX, direccionY, vida, partes[6], vidaMaxima, segundosParaReaparecer);
    }

    private void aplicarEstado(Jugador jugador, DatosJugador datos) {

        jugador.setPosicion(datos.x(), datos.y());

        // La vida llega calculada desde el servidor.
        // El cliente solamente actualiza el valor visual.
        jugador.setVida(datos.vida());
        jugador.setVidaMaxima(datos.vidaMaxima());
        jugador.setSegundosParaReaparecer(datos.segundosParaReaparecer());

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

            // SLOT,TIPO,NUMERO_ATAQUE,DIRECCION[,ALCANCE[,ORIGEN_X,ORIGEN_Y]]
            String[] partes = datosArma.split(",", -1);

            if (partes.length != 4 && partes.length != 5 && partes.length != 7) {
                continue;
            }

            try {
                int slot = Integer.parseInt(partes[0]);
                String tipo = partes[1];

                long numeroAtaque = Long.parseLong(partes[2]);
                int direccion = Integer.parseInt(partes[3]);

                float alcance = partes.length >= 5 ? Float.parseFloat(partes[4]) : 1f;

                if (slot < 0 || slot > 1 || numeroAtaque < 0 || direccion < 0 || direccion >= 8
                    || !Float.isFinite(alcance) || alcance < 1f) {
                    continue;
                }

                Arma arma = obtenerArma(jugador, slot, tipo);
                Long ultimoAtaque = ataquesJugador.get(slot);

                if (arma == null || numeroAtaque == 0 || (ultimoAtaque != null && numeroAtaque <= ultimoAtaque)) {
                    continue;
                }

                if (tipo.equals("ESPADA") && arma instanceof Espada espada && partes.length <= 5) {
                    espada.reproducirAtaque(jugador.getHitbox(), direccion, alcance);
                } else if (tipo.equals("PISTOLA") && arma instanceof Pistola pistola && partes.length == 7) {
                    float origenX = Float.parseFloat(partes[5]);
                    float origenY = Float.parseFloat(partes[6]);
                    if (!Float.isFinite(origenX) || !Float.isFinite(origenY)) {
                        continue;
                    }
                    pistola.reproducirAtaque(origenX, origenY, direccion, alcance);
                } else {
                    continue;
                }
                ataquesJugador.put(slot, numeroAtaque);

            } catch (NumberFormatException e) {
                Gdx.app.error("SincronizadorJugadores", "Ataque invalido: " + datosArma);
            }
        }
    }

    private Arma obtenerArma(Jugador jugador, int slot, String tipo) {
        List<Arma> armas = jugador.getArmas();
        if (slot < armas.size()) {
            return armas.get(slot);
        }

        Arma principal = armas.getFirst();
        Arma nueva;
        if (tipo.equals("PISTOLA") && principal instanceof Espada) {
            nueva = new Pistola();
        } else if (tipo.equals("ESPADA") && principal instanceof Pistola) {
            nueva = new Espada();
        } else {
            return null;
        }
        jugador.agregarArma(nueva);
        return nueva;
    }

    public boolean hayEstadoRecibido() {
        return estadoRecibido;
    }

    public void procesarEstadoHud(String mensaje) {
        if (mensaje == null) {
            return;
        }

        // HUD|SECUENCIA|SEGUNDOS|KILLS|PARTIDA|NIVEL|XP|META
        // Sufijo opcional: estado de la fase final o MS|OPCIONES|ELECCION_J1|ELECCION_J2.
        String[] partes = mensaje.split("\\|", -1);
        Resultado resultado = partes.length == 9 ? leerResultado(partes[8]) : Resultado.EN_CURSO;
        if ((partes.length != 4 && partes.length != 8 && partes.length != 12 && resultado == Resultado.EN_CURSO)
                || !ProtocoloRed.HUD.equals(partes[0])) {
            return;
        }

        try {
            long secuencia = Long.parseLong(partes[1]);
            int segundos = Integer.parseInt(partes[2]);
            int kills = Integer.parseInt(partes[3]);
            if (secuencia <= ultimaSecuenciaHud || segundos < 0 || kills < 0) {
                return;
            }

            String partidaId = partes.length >= 8 ? partes[4] : "";
            int nivel = partes.length >= 8 ? Integer.parseInt(partes[5]) : 1;
            int experiencia = partes.length >= 8 ? Integer.parseInt(partes[6]) : 0;
            int meta = partes.length >= 8 ? Integer.parseInt(partes[7]) : 1;
            if (nivel < 1 || experiencia < 0 || meta <= 0) {
                return;
            }

            RondaMejora ronda = null;
            if (partes.length == 12) {
                int milisegundos = Integer.parseInt(partes[8]);
                String[] opcionesPorJugador = partes[9].split(";", -1);
                if (opcionesPorJugador.length > 2) {
                    return;
                }
                List<String> opciones = List.of(opcionesPorJugador[opcionesPorJugador.length == 1 ? 0 : idJugadorLocal - 1].split(",", -1));
                String[] eleccion = partes[idJugadorLocal == 1 ? 10 : 11].split(",", -1);
                if (milisegundos <= 0 || opciones.size() != 3 || eleccion.length != 2) {
                    return;
                }

                int seleccion = Integer.parseInt(eleccion[0]);
                int revision = Integer.parseInt(eleccion[1]);
                if (seleccion < -1 || seleccion >= opciones.size() || revision < 0 || (seleccion == -1 && revision != 0)) {
                    return;
                }
                ronda = new RondaMejora(milisegundos, opciones, seleccion, revision);
            }

            ultimaSecuenciaHud = secuencia;
            estadisticasPartida = new EstadisticasPartida(segundos, kills, partidaId,
                nivel, experiencia, meta, ronda, resultado);
        } catch (NumberFormatException e) {
            Gdx.app.error("SincronizadorJugadores", "Estado HUD invalido: " + mensaje);
        }
    }

    private Resultado leerResultado(String codigo) {
        return switch (codigo) {
            case "FASE_FINAL" -> Resultado.FASE_FINAL;
            case "VICTORIA" -> Resultado.VICTORIA_COOPERATIVA;
            case "VICTORIA_1" -> idJugadorLocal == 1 ? Resultado.VICTORIA_LOCAL : Resultado.VICTORIA_REMOTA;
            case "VICTORIA_2" -> idJugadorLocal == 2 ? Resultado.VICTORIA_LOCAL : Resultado.VICTORIA_REMOTA;
            default -> Resultado.EN_CURSO;
        };
    }

    public enum Resultado {
        EN_CURSO, FASE_FINAL, VICTORIA_LOCAL, VICTORIA_REMOTA, VICTORIA_COOPERATIVA
    }

    public EstadisticasPartida getEstadisticasPartida() {
        return estadisticasPartida;
    }

    public record EstadisticasPartida(int segundos, int kills, String partidaId, int nivel,
                                      int experiencia, int meta, RondaMejora rondaMejora, Resultado resultado) {
    }

    public record RondaMejora(int milisegundos, List<String> opciones, int seleccion, int revision) {
    }

    private record DatosJugador(int id, float x, float y, float direccionX, float direccionY, float vida,
                                String armas, float vidaMaxima, int segundosParaReaparecer) {
    }
}
