package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

record EstadoEnemigos(long secuencia, List<DatosEnemigo> enemigos, Set<Integer> ids,
                      List<CirculoFuego> fuego, List<Bala> balas) {
    private static final Set<String> TIPOS_ENEMIGOS = Set.of("BASICO", "VIOLETA", "JEFE", "MUERTE");

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

    record CirculoFuego(float x, float y, float radio, float opacidad) {
    }

    record Bala(float x, float y, float angulo) {
    }

    record DatosEnemigo(int id, String tipo, float x, float y, int vida, int vidaMaxima) {
    }
}
