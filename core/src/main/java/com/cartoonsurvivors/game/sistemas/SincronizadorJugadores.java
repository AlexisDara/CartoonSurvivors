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
import java.util.Objects;

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

            if (datos1.getId() != 1 || datos2.getId() != 2) {
                return;
            }

            if (idJugadorLocal == 1) {
                aplicarEstado(jugadorLocal, datos1);
                aplicarEstado(jugadorRemoto, datos2);
                pasivasLocales = datos1.getPasivas();
            } else {
                aplicarEstado(jugadorLocal, datos2);
                aplicarEstado(jugadorRemoto, datos1);
                pasivasLocales = datos2.getPasivas();
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
        float vidaMaxima;
        if (partes.length >= 8) {
            vidaMaxima = Float.parseFloat(partes[7]);
        } else {
            vidaMaxima = Constantes.Jugador.VIDA_INICIAL;
        }
        int segundosParaReaparecer;
        if (partes.length >= 9) {
            segundosParaReaparecer = Integer.parseInt(partes[8]);
        } else {
            segundosParaReaparecer = 0;
        }

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

        jugador.setPosicion(datos.getX(), datos.getY());

        // La vida llega calculada desde el servidor.
        // El cliente solamente actualiza el valor visual.
        jugador.setVida(datos.getVida());
        jugador.setVidaMaxima(datos.getVidaMaxima());
        jugador.setSegundosParaReaparecer(datos.getSegundosParaReaparecer());

        jugador.seMueve(datos.getDireccionX(), datos.getDireccionY());

        jugador.calcularLadoMirada(datos.getLado());

        actualizarArmas(jugador, datos.getArmas());
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

                float alcance;
                if (partes.length >= 5) {
                    alcance = Float.parseFloat(partes[4]);
                } else {
                    alcance = 1f;
                }

                if (slot < 0 || slot > 1 || numeroAtaque < 0 || direccion < 0 || direccion >= 8
                    || !Float.isFinite(alcance) || alcance < 1f) {
                    continue;
                }

                Arma arma = obtenerArma(jugador, slot, tipo);
                Long ultimoAtaque = ataquesJugador.get(slot);

                if (arma == null || numeroAtaque == 0 || (ultimoAtaque != null && numeroAtaque <= ultimoAtaque)) {
                    continue;
                }

                if (tipo.equals("ESPADA") && arma instanceof Espada && partes.length <= 5) {
                    Espada espada = (Espada) arma;
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

        Arma principal = armas.get(0);
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
        Resultado resultado;
        if (partes.length == 9) {
            resultado = leerResultado(partes[8]);
        } else {
            resultado = Resultado.EN_CURSO;
        }
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

            String partidaId = "";
            int nivel = 1;
            int experiencia = 0;
            int meta = 1;
            if (partes.length >= 8) {
                partidaId = partes[4];
                nivel = Integer.parseInt(partes[5]);
                experiencia = Integer.parseInt(partes[6]);
                meta = Integer.parseInt(partes[7]);
            }
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
                int indiceOpciones;
                if (opcionesPorJugador.length == 1) {
                    indiceOpciones = 0;
                } else {
                    indiceOpciones = idJugadorLocal - 1;
                }
                ArrayList<OpcionMejora> opcionesLeidas = new ArrayList<>();
                for (String opcion : opcionesPorJugador[indiceOpciones].split(",", -1)) {
                    opcionesLeidas.add(OpcionMejora.leer(opcion));
                }
                List<OpcionMejora> opciones = List.copyOf(opcionesLeidas);
                int indiceEleccion;
                if (idJugadorLocal == 1) {
                    indiceEleccion = 10;
                } else {
                    indiceEleccion = 11;
                }
                String[] eleccion = partes[indiceEleccion].split(",", -1);
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

            Pausa pausa;
            if (datosPausa == null) {
                pausa = null;
            } else {
                pausa = leerPausa(datosPausa);
            }
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
        switch (codigo) {
            case "FASE_FINAL":
                return Resultado.FASE_FINAL;
            case "VICTORIA":
                return Resultado.VICTORIA;
            case "DERROTA":
                return Resultado.DERROTA;
            default:
                return Resultado.EN_CURSO;
        }
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

    public static final class EstadisticasPartida {
        private final int segundos;
        private final int kills;
        private final String partidaId;
        private final int nivel;
        private final int experiencia;
        private final int meta;
        private final RondaMejora rondaMejora;
        private final Resultado resultado;
        private final Pausa pausa;

        public EstadisticasPartida(int segundos, int kills, String partidaId, int nivel, int experiencia, int meta, RondaMejora rondaMejora, Resultado resultado, Pausa pausa) {
            this.segundos = segundos;
            this.kills = kills;
            this.partidaId = partidaId;
            this.nivel = nivel;
            this.experiencia = experiencia;
            this.meta = meta;
            this.rondaMejora = rondaMejora;
            this.resultado = resultado;
            this.pausa = pausa;
        }

        public int getSegundos() {
            return segundos;
        }

        public int getKills() {
            return kills;
        }

        public String getPartidaId() {
            return partidaId;
        }

        public int getNivel() {
            return nivel;
        }

        public int getExperiencia() {
            return experiencia;
        }

        public int getMeta() {
            return meta;
        }

        public RondaMejora getRondaMejora() {
            return rondaMejora;
        }

        public Resultado getResultado() {
            return resultado;
        }

        public Pausa getPausa() {
            return pausa;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof EstadisticasPartida)) {
                return false;
            }
            EstadisticasPartida otra = (EstadisticasPartida) objeto;
            return segundos == otra.segundos
                && kills == otra.kills
                && Objects.equals(partidaId, otra.partidaId)
                && nivel == otra.nivel
                && experiencia == otra.experiencia
                && meta == otra.meta
                && Objects.equals(rondaMejora, otra.rondaMejora)
                && Objects.equals(resultado, otra.resultado)
                && Objects.equals(pausa, otra.pausa);
        }

        @Override
        public int hashCode() {
            int resultado = Integer.hashCode(segundos);
            resultado = 31 * resultado + Integer.hashCode(kills);
            resultado = 31 * resultado + Objects.hashCode(partidaId);
            resultado = 31 * resultado + Integer.hashCode(nivel);
            resultado = 31 * resultado + Integer.hashCode(experiencia);
            resultado = 31 * resultado + Integer.hashCode(meta);
            resultado = 31 * resultado + Objects.hashCode(rondaMejora);
            resultado = 31 * resultado + Objects.hashCode(resultado);
            resultado = 31 * resultado + Objects.hashCode(pausa);
            return resultado;
        }

        @Override
        public String toString() {
            return "EstadisticasPartida[segundos=" + segundos
                + ", kills=" + kills
                + ", partidaId=" + partidaId
                + ", nivel=" + nivel
                + ", experiencia=" + experiencia
                + ", meta=" + meta
                + ", rondaMejora=" + rondaMejora
                + ", resultado=" + resultado
                + ", pausa=" + pausa
                + "]";
        }
    }

    public static final class Pausa {
        private final int revision;
        private final int solicitante;
        private final int milisegundos;
        private final int confirmaciones;

        public Pausa(int revision, int solicitante, int milisegundos, int confirmaciones) {
            this.revision = revision;
            this.solicitante = solicitante;
            this.milisegundos = milisegundos;
            this.confirmaciones = confirmaciones;
        }

        public int getRevision() {
            return revision;
        }

        public int getSolicitante() {
            return solicitante;
        }

        public int getMilisegundos() {
            return milisegundos;
        }

        public int getConfirmaciones() {
            return confirmaciones;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof Pausa)) {
                return false;
            }
            Pausa otra = (Pausa) objeto;
            return revision == otra.revision
                && solicitante == otra.solicitante
                && milisegundos == otra.milisegundos
                && confirmaciones == otra.confirmaciones;
        }

        @Override
        public int hashCode() {
            int resultado = Integer.hashCode(revision);
            resultado = 31 * resultado + Integer.hashCode(solicitante);
            resultado = 31 * resultado + Integer.hashCode(milisegundos);
            resultado = 31 * resultado + Integer.hashCode(confirmaciones);
            return resultado;
        }

        @Override
        public String toString() {
            return "Pausa[revision=" + revision
                + ", solicitante=" + solicitante
                + ", milisegundos=" + milisegundos
                + ", confirmaciones=" + confirmaciones
                + "]";
        }
        public boolean activa() {
            return milisegundos > 0;
        }

        public boolean confirmado(int jugador) {
            return (confirmaciones & (1 << (jugador - 1))) != 0;
        }
    }

    public static final class RondaMejora {
        private final int milisegundos;
        private final List<OpcionMejora> opciones;
        private final int seleccion;
        private final int revision;

        public RondaMejora(int milisegundos, List<OpcionMejora> opciones, int seleccion, int revision) {
            this.milisegundos = milisegundos;
            this.opciones = opciones;
            this.seleccion = seleccion;
            this.revision = revision;
        }

        public int getMilisegundos() {
            return milisegundos;
        }

        public List<OpcionMejora> getOpciones() {
            return opciones;
        }

        public int getSeleccion() {
            return seleccion;
        }

        public int getRevision() {
            return revision;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof RondaMejora)) {
                return false;
            }
            RondaMejora otra = (RondaMejora) objeto;
            return milisegundos == otra.milisegundos
                && Objects.equals(opciones, otra.opciones)
                && seleccion == otra.seleccion
                && revision == otra.revision;
        }

        @Override
        public int hashCode() {
            int resultado = Integer.hashCode(milisegundos);
            resultado = 31 * resultado + Objects.hashCode(opciones);
            resultado = 31 * resultado + Integer.hashCode(seleccion);
            resultado = 31 * resultado + Integer.hashCode(revision);
            return resultado;
        }

        @Override
        public String toString() {
            return "RondaMejora[milisegundos=" + milisegundos
                + ", opciones=" + opciones
                + ", seleccion=" + seleccion
                + ", revision=" + revision
                + "]";
        }
    }

    public static final class OpcionMejora {
        private final String tipo;
        private final List<Integer> valores;

        public OpcionMejora(String tipo, List<Integer> valores) {
            this.tipo = tipo;
            this.valores = valores;
        }

        public String getTipo() {
            return tipo;
        }

        public List<Integer> getValores() {
            return valores;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof OpcionMejora)) {
                return false;
            }
            OpcionMejora otra = (OpcionMejora) objeto;
            return Objects.equals(tipo, otra.tipo)
                && Objects.equals(valores, otra.valores);
        }

        @Override
        public int hashCode() {
            int resultado = Objects.hashCode(tipo);
            resultado = 31 * resultado + Objects.hashCode(valores);
            return resultado;
        }

        @Override
        public String toString() {
            return "OpcionMejora[tipo=" + tipo
                + ", valores=" + valores
                + "]";
        }
        private static OpcionMejora leer(String codigo) {
            String[] campos = codigo.split(":", -1);
            int cantidad;
            switch (campos[0]) {
                case "ESPADA":
                    cantidad = 3;
                    break;
                case "PISTOLA":
                    if (campos.length == 6) {
                        cantidad = 5;
                    } else if (campos.length == 5 && campos[4].equals("1")) {
                        cantidad = 4;
                    } else {
                        cantidad = 3;
                    }
                    break;
                case "VIDA_MAXIMA":
                    cantidad = 2;
                    break;
                case "VELOCIDAD":
                case "REGENERACION":
                case "ARMADURA":
                case "CRITICO":
                    cantidad = 1;
                    break;
                case "ARMA":
                    cantidad = 0;
                    break;
                default:
                    throw new NumberFormatException("Tipo de mejora desconocido");
            }
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

    public static final class Pasivas {
        private final float vidaMaxima;
        private final float velocidad;
        private final float regeneracion;
        private final int armadura;
        private final int critico;

        public Pasivas(float vidaMaxima, float velocidad, float regeneracion, int armadura, int critico) {
            this.vidaMaxima = vidaMaxima;
            this.velocidad = velocidad;
            this.regeneracion = regeneracion;
            this.armadura = armadura;
            this.critico = critico;
        }

        public float getVidaMaxima() {
            return vidaMaxima;
        }

        public float getVelocidad() {
            return velocidad;
        }

        public float getRegeneracion() {
            return regeneracion;
        }

        public int getArmadura() {
            return armadura;
        }

        public int getCritico() {
            return critico;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof Pasivas)) {
                return false;
            }
            Pasivas otra = (Pasivas) objeto;
            return Float.compare(vidaMaxima, otra.vidaMaxima) == 0
                && Float.compare(velocidad, otra.velocidad) == 0
                && Float.compare(regeneracion, otra.regeneracion) == 0
                && armadura == otra.armadura
                && critico == otra.critico;
        }

        @Override
        public int hashCode() {
            int resultado = Float.hashCode(vidaMaxima);
            resultado = 31 * resultado + Float.hashCode(velocidad);
            resultado = 31 * resultado + Float.hashCode(regeneracion);
            resultado = 31 * resultado + Integer.hashCode(armadura);
            resultado = 31 * resultado + Integer.hashCode(critico);
            return resultado;
        }

        @Override
        public String toString() {
            return "Pasivas[vidaMaxima=" + vidaMaxima
                + ", velocidad=" + velocidad
                + ", regeneracion=" + regeneracion
                + ", armadura=" + armadura
                + ", critico=" + critico
                + "]";
        }
    }

    private static final class DatosJugador {
        private final int id;
        private final float x;
        private final float y;
        private final float direccionX;
        private final float direccionY;
        private final float vida;
        private final String armas;
        private final float vidaMaxima;
        private final int segundosParaReaparecer;
        private final float lado;
        private final Pasivas pasivas;

        DatosJugador(int id, float x, float y, float direccionX, float direccionY, float vida, String armas, float vidaMaxima, int segundosParaReaparecer, float lado, Pasivas pasivas) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.direccionX = direccionX;
            this.direccionY = direccionY;
            this.vida = vida;
            this.armas = armas;
            this.vidaMaxima = vidaMaxima;
            this.segundosParaReaparecer = segundosParaReaparecer;
            this.lado = lado;
            this.pasivas = pasivas;
        }

        public int getId() {
            return id;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getDireccionX() {
            return direccionX;
        }

        public float getDireccionY() {
            return direccionY;
        }

        public float getVida() {
            return vida;
        }

        public String getArmas() {
            return armas;
        }

        public float getVidaMaxima() {
            return vidaMaxima;
        }

        public int getSegundosParaReaparecer() {
            return segundosParaReaparecer;
        }

        public float getLado() {
            return lado;
        }

        public Pasivas getPasivas() {
            return pasivas;
        }
    }
}
