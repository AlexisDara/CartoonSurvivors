package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.armas.Pistola;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.red.ProtocoloRed;
import com.cartoonsurvivors.game.utilidades.Constantes;

import java.util.ArrayList;
import java.util.Arrays;
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
    private Pasivas pasivasLocales;
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
                pasivasLocales = datos1.pasivas();
            } else {
                aplicarEstado(jugadorLocal, datos2);
                aplicarEstado(jugadorRemoto, datos1);
                pasivasLocales = datos2.pasivas();
            }

            ultimaSecuencia = secuencia;
            estadoRecibido = true;

        } catch (NumberFormatException e) {
            Gdx.app.error("SincronizadorJugadores", "Estado invalido: " + mensaje);
        }
    }

    private DatosJugador interpretarJugador(String datos) {

        // ID:X:Y:DX:DY:VIDA:ARMAS:VIDA_MAXIMA:REAPARICION:VELOCIDAD:REGEN:ARMADURA:CRITICO:LADO
        String[] partes = datos.split(":", -1);

        if (partes.length < 7 || (partes.length > 9 && partes.length != 14)) {
            return null;
        }

        int id = Integer.parseInt(partes[0]);

        float x = Float.parseFloat(partes[1]);
        float y = Float.parseFloat(partes[2]);

        float direccionX = Float.parseFloat(partes[3]);
        float direccionY = Float.parseFloat(partes[4]);

        float vida = Float.parseFloat(partes[5]);
        float vidaMaxima = partes.length >= 8 ? Float.parseFloat(partes[7]) : Constantes.Jugador.VIDA_INICIAL;
        int segundosParaReaparecer = partes.length >= 9 ? Integer.parseInt(partes[8]) : 0;

        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(direccionX) || !Float.isFinite(direccionY)
            || !Float.isFinite(vida) || !Float.isFinite(vidaMaxima) || vidaMaxima <= 0 || vida < 0 || vida > vidaMaxima
            || segundosParaReaparecer < 0) {
            return null;
        }

        Pasivas pasivas = null;
        float lado = direccionX;
        if (partes.length == 14) {
            float velocidad = Float.parseFloat(partes[9]);
            float regeneracion = Float.parseFloat(partes[10]);
            int armadura = Integer.parseInt(partes[11]);
            int critico = Integer.parseInt(partes[12]);
            lado = Integer.parseInt(partes[13]);
            if (!Float.isFinite(velocidad) || velocidad <= 0f || !Float.isFinite(regeneracion) || regeneracion < 0f
                || armadura < 0 || armadura > 80 || critico < 0 || critico > 100 || Math.abs(lado) != 1f) {
                return null;
            }
            pasivas = new Pasivas(vidaMaxima, velocidad, regeneracion, armadura, critico);
        }
        return new DatosJugador(id, x, y, direccionX, direccionY, vida, partes[6], vidaMaxima,
            segundosParaReaparecer, lado, pasivas);
    }

    private void aplicarEstado(Jugador jugador, DatosJugador datos) {

        jugador.setPosicion(datos.x(), datos.y());

        // La vida llega calculada desde el servidor.
        // El cliente solamente actualiza el valor visual.
        jugador.setVida(datos.vida());
        jugador.setVidaMaxima(datos.vidaMaxima());
        jugador.setSegundosParaReaparecer(datos.segundosParaReaparecer());

        jugador.seMueve(datos.direccionX(), datos.direccionY());

        jugador.calcularLadoMirada(datos.lado());

        actualizarArmas(jugador, datos.armas());
    }

    private void actualizarArmas(Jugador jugador, String datosArmas) {

        if (datosArmas.isEmpty()) {
            return;
        }

        Map<Integer, Long> ataquesJugador = ultimosAtaques.get(jugador);
        if (ataquesJugador == null) {
            ataquesJugador = new HashMap<>();
            ultimosAtaques.put(jugador, ataquesJugador);
        }

        String[] armasRecibidas = datosArmas.split(";");

        for (String datosArma : armasRecibidas) {

            // SLOT,TIPO,NUMERO_ATAQUE,DIRECCION[,ALCANCE]
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
                    ataquesJugador.put(slot, numeroAtaque);
                }

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

    public Pasivas getPasivasLocales() {
        return pasivasLocales;
    }

    public void procesarEstadoHud(String mensaje) {
        if (mensaje == null) {
            return;
        }

        // HUD|SECUENCIA|SEGUNDOS|KILLS|PARTIDA|NIVEL|XP|META
        // Tras el resultado o la ronda de mejoras puede venir PAUSA:REVISION:JUGADOR:MS:CONFIRMACIONES.
        String[] partes = mensaje.split("\\|", -1);
        String datosPausa = partes[partes.length - 1];
        if (datosPausa.startsWith(ProtocoloRed.PAUSA + ":")) {
            partes = Arrays.copyOf(partes, partes.length - 1);
        } else {
            datosPausa = null;
        }
        if (partes.length == 0) {
            return;
        }
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
                int indiceOpciones = opcionesPorJugador.length == 1 ? 0 : idJugadorLocal - 1;
                ArrayList<OpcionMejora> opcionesLeidas = new ArrayList<>();
                for (String opcion : opcionesPorJugador[indiceOpciones].split(",", -1)) {
                    opcionesLeidas.add(OpcionMejora.leer(opcion));
                }
                List<OpcionMejora> opciones = List.copyOf(opcionesLeidas);
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

            Pausa pausa = datosPausa == null ? null : leerPausa(datosPausa);
            ultimaSecuenciaHud = secuencia;
            estadisticasPartida = new EstadisticasPartida(segundos, kills, partidaId,
                nivel, experiencia, meta, ronda, resultado, pausa);
        } catch (NumberFormatException e) {
            Gdx.app.error("SincronizadorJugadores", "Estado HUD invalido: " + mensaje);
        }
    }

    private Pausa leerPausa(String mensaje) {
        String[] campos = mensaje.split(":", -1);
        if (campos.length != 5) {
            throw new NumberFormatException("Formato de pausa inválido");
        }
        int revision = Integer.parseInt(campos[1]);
        int solicitante = Integer.parseInt(campos[2]);
        int milisegundos = Integer.parseInt(campos[3]);
        int confirmaciones = Integer.parseInt(campos[4]);
        if (revision < 0 || solicitante < 0 || solicitante > 2 || milisegundos < 0 || milisegundos > 40_000
            || confirmaciones < 0 || confirmaciones > 2 || (milisegundos == 0) != (solicitante == 0)
            || (milisegundos == 0 && confirmaciones != 0)) {
            throw new NumberFormatException("Estado de pausa inválido");
        }
        return new Pausa(revision, solicitante, milisegundos, confirmaciones);
    }

    private Resultado leerResultado(String codigo) {
        return switch (codigo) {
            case "FASE_FINAL" -> Resultado.FASE_FINAL;
            case "VICTORIA" -> Resultado.VICTORIA;
            case "DERROTA" -> Resultado.DERROTA;
            default -> Resultado.EN_CURSO;
        };
    }

    public enum Resultado {
        EN_CURSO, FASE_FINAL, VICTORIA, DERROTA;

        public boolean esFinal() {
            return this == VICTORIA || this == DERROTA;
        }
    }

    public EstadisticasPartida getEstadisticasPartida() {
        return estadisticasPartida;
    }

    public record EstadisticasPartida(int segundos, int kills, String partidaId, int nivel,
                                      int experiencia, int meta, RondaMejora rondaMejora, Resultado resultado,
                                      Pausa pausa) {
    }

    public record Pausa(int revision, int solicitante, int milisegundos, int confirmaciones) {
        public boolean activa() {
            return milisegundos > 0;
        }

        public boolean confirmado(int jugador) {
            return (confirmaciones & (1 << (jugador - 1))) != 0;
        }
    }

    public record RondaMejora(int milisegundos, List<OpcionMejora> opciones, int seleccion, int revision) {
    }

    public record OpcionMejora(String tipo, List<Integer> valores) {
        private static OpcionMejora leer(String codigo) {
            String[] campos = codigo.split(":", -1);
            int cantidad = switch (campos[0]) {
                case "ESPADA" -> 3;
                case "PISTOLA" -> switch (campos.length) {
                    case 6 -> 5;
                    case 5 -> campos[4].equals("1") ? 4 : 3;
                    default -> 3;
                };
                case "VIDA_MAXIMA" -> 2;
                case "VELOCIDAD", "REGENERACION", "ARMADURA", "CRITICO" -> 1;
                case "ARMA" -> 0;
                default -> throw new NumberFormatException("Tipo de mejora desconocido");
            };
            if (campos.length != cantidad + 1) {
                throw new NumberFormatException("Cantidad de valores de mejora inválida");
            }
            ArrayList<Integer> valores = new ArrayList<>();
            for (int i = 1; i < campos.length; i++) {
                valores.add(Integer.parseInt(campos[i]));
            }
            return new OpcionMejora(campos[0], List.copyOf(valores));
        }

        public int valor(int indice) {
            return valores.get(indice);
        }
    }

    public record Pasivas(float vidaMaxima, float velocidad, float regeneracion, int armadura, int critico) {
    }

    private record DatosJugador(int id, float x, float y, float direccionX, float direccionY, float vida,
                                String armas, float vidaMaxima, int segundosParaReaparecer, float lado, Pasivas pasivas) {
    }
}
