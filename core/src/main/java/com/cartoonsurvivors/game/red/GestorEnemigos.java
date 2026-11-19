package com.cartoonsurvivors.game.red;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorEnemigos {

    private static final int MAX_BLOQUES_ENEMIGOS = 128;
    private static final int MAX_ESTADOS_PENDIENTES = 8;

    private final Map<Long, EstadoEnemigosPendiente> estadosPendientes = new HashMap<>();

    private String ultimoEstadoEnemigos;
    private long ultimaSecuenciaCompleta = -1;

    public synchronized void recibirBloque(String mensaje) {

        String[] partes = mensaje.split("\\|", 5);

        if (partes.length < 4) {
            return;
        }

        try {
            long secuencia = Long.parseLong(partes[1]);
            int indice = Integer.parseInt(partes[2]);
            int total = Integer.parseInt(partes[3]);

            if (secuencia <= ultimaSecuenciaCompleta) {
                return;
            }

            if (secuencia < 0 || total <= 0 || total > MAX_BLOQUES_ENEMIGOS || indice < 0 || indice >= total) {
                return;
            }

            EstadoEnemigosPendiente pendiente = estadosPendientes.get(secuencia);

            if (pendiente == null) {
                pendiente = new EstadoEnemigosPendiente(total);
                estadosPendientes.put(secuencia, pendiente);
            }

            if (pendiente.getTotalBloques() != total) {
                return;
            }

            String datos = "";

            if (partes.length == 5) {
                datos = "|" + partes[4];
            }

            pendiente.agregarBloque(indice, datos);

            while (estadosPendientes.size() > MAX_ESTADOS_PENDIENTES) {

                long masAntiguo = Long.MAX_VALUE;

                for (long numero : estadosPendientes.keySet()) {
                    if (numero < masAntiguo) {
                        masAntiguo = numero;
                    }
                }

                estadosPendientes.remove(masAntiguo);
            }

            if (!pendiente.estaCompleto()) {
                return;
            }

            ultimoEstadoEnemigos = pendiente.reconstruir(secuencia);
            ultimaSecuenciaCompleta = secuencia;

            List<Long> secuencias = new ArrayList<>(estadosPendientes.keySet());

            for (long numero : secuencias) {
                if (numero <= ultimaSecuenciaCompleta) {
                    estadosPendientes.remove(numero);
                }
            }

        } catch (NumberFormatException e) {
            // Ignorar paquetes con datos numéricos inválidos.
        }
    }

    public synchronized String consumirEstadoEnemigos() {

        String mensaje = ultimoEstadoEnemigos;
        ultimoEstadoEnemigos = null;

        return mensaje;
    }

    public synchronized void reiniciar() {

        ultimoEstadoEnemigos = null;
        ultimaSecuenciaCompleta = -1;
        estadosPendientes.clear();
    }

    private static class EstadoEnemigosPendiente {

        private final String[] bloques;
        private int cantidadRecibida;

        public EstadoEnemigosPendiente(int totalBloques) {
            bloques = new String[totalBloques];
            cantidadRecibida = 0;
        }

        public void agregarBloque(int indice, String datos) {

            if (bloques[indice] == null) {
                bloques[indice] = datos;
                cantidadRecibida++;
            }
        }

        public boolean estaCompleto() {
            return cantidadRecibida == bloques.length;
        }

        public int getTotalBloques() {
            return bloques.length;
        }

        public String reconstruir(long secuencia) {

            StringBuilder mensaje = new StringBuilder("ENEMIGOS");

            mensaje.append("|").append(secuencia);

            for (String bloque : bloques) {
                mensaje.append(bloque);
            }

            return mensaje.toString();
        }
    }
}
