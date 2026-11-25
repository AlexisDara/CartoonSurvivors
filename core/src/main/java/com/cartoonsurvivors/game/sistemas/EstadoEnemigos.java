package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class EstadoEnemigos {
    private static final Set<String> TIPOS_ENEMIGOS = Set.of("BASICO", "VIOLETA", "JEFE", "MUERTE");

    private final long secuencia;
    private final List<DatosEnemigo> enemigos;
    private final Set<Integer> ids;
    private final List<CirculoFuego> fuego;
    private final List<Bala> balas;

    EstadoEnemigos(long secuencia, List<DatosEnemigo> enemigos, Set<Integer> ids, List<CirculoFuego> fuego, List<Bala> balas) {
        this.secuencia = secuencia;
        this.enemigos = enemigos;
        this.ids = ids;
        this.fuego = fuego;
        this.balas = balas;
    }

    public long getSecuencia() {
        return secuencia;
    }

    public List<DatosEnemigo> getEnemigos() {
        return enemigos;
    }

    public Set<Integer> getIds() {
        return ids;
    }

    public List<CirculoFuego> getFuego() {
        return fuego;
    }

    public List<Bala> getBalas() {
        return balas;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof EstadoEnemigos)) {
            return false;
        }
        EstadoEnemigos otra = (EstadoEnemigos) objeto;
        return secuencia == otra.secuencia
            && Objects.equals(enemigos, otra.enemigos)
            && Objects.equals(ids, otra.ids)
            && Objects.equals(fuego, otra.fuego)
            && Objects.equals(balas, otra.balas);
    }

    @Override
    public int hashCode() {
        int resultado = Long.hashCode(secuencia);
        resultado = 31 * resultado + Objects.hashCode(enemigos);
        resultado = 31 * resultado + Objects.hashCode(ids);
        resultado = 31 * resultado + Objects.hashCode(fuego);
        resultado = 31 * resultado + Objects.hashCode(balas);
        return resultado;
    }

    @Override
    public String toString() {
        return "EstadoEnemigos[secuencia=" + secuencia
            + ", enemigos=" + enemigos
            + ", ids=" + ids
            + ", fuego=" + fuego
            + ", balas=" + balas
            + "]";
    }

    static EstadoEnemigos leer(String mensaje, long ultimaSecuencia) {

        if (mensaje == null) {
            return null;
        }

        // ENEMIGOS|SECUENCIA|ID:TIPO:X:Y:VIDA[:VIDA_MAXIMA]|FUEGO:X:Y:RADIO:OPACIDAD|BALA:X:Y:ANGULO|...
        String[] partes = mensaje.split("\\|", -1);

        if (partes.length < 2 || !partes[0].equals("ENEMIGOS")) {
            return null;
        }

        try {
            long secuencia = Long.parseLong(partes[1]);

            if (secuencia <= ultimaSecuencia) {
                return null;
            }

            List<DatosEnemigo> recibidos = new ArrayList<>();
            List<CirculoFuego> fuegoRecibido = new ArrayList<>();
            List<Bala> balasRecibidas = new ArrayList<>();
            Set<Integer> idsRecibidos = new HashSet<>();

            // Validar todo el estado antes de aplicarlo.
            for (int i = 2; i < partes.length; i++) {

                String[] datos = partes[i].split(":", -1);

                if (datos[0].equals("BALA")) {
                    if (datos.length != 4) {
                        return null;
                    }
                    float x = Float.parseFloat(datos[1]);
                    float y = Float.parseFloat(datos[2]);
                    float angulo = Float.parseFloat(datos[3]);
                    if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(angulo)) {
                        return null;
                    }
                    balasRecibidas.add(new Bala(x, y, angulo));
                    continue;
                }

                if (datos.length != 5 && datos.length != 6) {
                    return null;
                }

                if (datos[0].equals("FUEGO")) {
                    if (datos.length != 5) {
                        return null;
                    }
                    float x = Float.parseFloat(datos[1]);
                    float y = Float.parseFloat(datos[2]);
                    float radio = Float.parseFloat(datos[3]);
                    float opacidad = Float.parseFloat(datos[4]);
                    if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(radio) || radio <= 0f
                            || !Float.isFinite(opacidad) || opacidad < 0f || opacidad > 1f) {
                        return null;
                    }
                    fuegoRecibido.add(new CirculoFuego(x, y, radio, opacidad));
                    continue;
                }

                int id = Integer.parseInt(datos[0]);
                String tipo = datos[1];

                float x = Float.parseFloat(datos[2]);
                float y = Float.parseFloat(datos[3]);
                int vida = Integer.parseInt(datos[4]);
                int vidaMaxima = 0;
                if (datos.length == 6) {
                    vidaMaxima = Integer.parseInt(datos[5]);
                    if (!tipo.equals("JEFE") || vidaMaxima <= 0 || vida > vidaMaxima) {
                        return null;
                    }
                }

                if (id <= 0 || !TIPOS_ENEMIGOS.contains(tipo) || !Float.isFinite(x) || !Float.isFinite(y)
                        || vida < 0 || !idsRecibidos.add(id)) {
                    return null;
                }

                recibidos.add(new DatosEnemigo(id, tipo, x, y, vida, vidaMaxima));
            }

            return new EstadoEnemigos(secuencia, recibidos, idsRecibidos, fuegoRecibido, balasRecibidas);
        } catch (NumberFormatException e) {
            Gdx.app.error("EstadoEnemigos", "Estado de enemigos inválido: " + mensaje);
            return null;
        }
    }

    static final class CirculoFuego {
        private final float x;
        private final float y;
        private final float radio;
        private final float opacidad;

        CirculoFuego(float x, float y, float radio, float opacidad) {
            this.x = x;
            this.y = y;
            this.radio = radio;
            this.opacidad = opacidad;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getRadio() {
            return radio;
        }

        public float getOpacidad() {
            return opacidad;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof CirculoFuego)) {
                return false;
            }
            CirculoFuego otro = (CirculoFuego) objeto;
            return Float.compare(x, otro.x) == 0
                && Float.compare(y, otro.y) == 0
                && Float.compare(radio, otro.radio) == 0
                && Float.compare(opacidad, otro.opacidad) == 0;
        }

        @Override
        public int hashCode() {
            int resultado = Float.hashCode(x);
            resultado = 31 * resultado + Float.hashCode(y);
            resultado = 31 * resultado + Float.hashCode(radio);
            resultado = 31 * resultado + Float.hashCode(opacidad);
            return resultado;
        }

        @Override
        public String toString() {
            return "CirculoFuego[x=" + x
                + ", y=" + y
                + ", radio=" + radio
                + ", opacidad=" + opacidad
                + "]";
        }
    }

    static final class Bala {
        private final float x;
        private final float y;
        private final float angulo;

        Bala(float x, float y, float angulo) {
            this.x = x;
            this.y = y;
            this.angulo = angulo;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAngulo() {
            return angulo;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof Bala)) {
                return false;
            }
            Bala otro = (Bala) objeto;
            return Float.compare(x, otro.x) == 0
                && Float.compare(y, otro.y) == 0
                && Float.compare(angulo, otro.angulo) == 0;
        }

        @Override
        public int hashCode() {
            int resultado = Float.hashCode(x);
            resultado = 31 * resultado + Float.hashCode(y);
            resultado = 31 * resultado + Float.hashCode(angulo);
            return resultado;
        }

        @Override
        public String toString() {
            return "Bala[x=" + x
                + ", y=" + y
                + ", angulo=" + angulo
                + "]";
        }
    }

    static final class DatosEnemigo {
        private final int id;
        private final String tipo;
        private final float x;
        private final float y;
        private final int vida;
        private final int vidaMaxima;

        DatosEnemigo(int id, String tipo, float x, float y, int vida, int vidaMaxima) {
            this.id = id;
            this.tipo = tipo;
            this.x = x;
            this.y = y;
            this.vida = vida;
            this.vidaMaxima = vidaMaxima;
        }

        public int getId() {
            return id;
        }

        public String getTipo() {
            return tipo;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getVida() {
            return vida;
        }

        public int getVidaMaxima() {
            return vidaMaxima;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof DatosEnemigo)) {
                return false;
            }
            DatosEnemigo otro = (DatosEnemigo) objeto;
            return id == otro.id
                && Objects.equals(tipo, otro.tipo)
                && Float.compare(x, otro.x) == 0
                && Float.compare(y, otro.y) == 0
                && vida == otro.vida
                && vidaMaxima == otro.vidaMaxima;
        }

        @Override
        public int hashCode() {
            int resultado = Integer.hashCode(id);
            resultado = 31 * resultado + Objects.hashCode(tipo);
            resultado = 31 * resultado + Float.hashCode(x);
            resultado = 31 * resultado + Float.hashCode(y);
            resultado = 31 * resultado + Integer.hashCode(vida);
            resultado = 31 * resultado + Integer.hashCode(vidaMaxima);
            return resultado;
        }

        @Override
        public String toString() {
            return "DatosEnemigo[id=" + id
                + ", tipo=" + tipo
                + ", x=" + x
                + ", y=" + y
                + ", vida=" + vida
                + ", vidaMaxima=" + vidaMaxima
                + "]";
        }
    }
}
