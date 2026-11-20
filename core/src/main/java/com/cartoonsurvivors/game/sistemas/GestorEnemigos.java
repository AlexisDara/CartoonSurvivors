package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestorEnemigos {

    private final Map<Integer, EnemigoBasico> enemigos = new HashMap<>();

    private final Texture texturaEnemigos;

    private long ultimaSecuencia = -1;

    public GestorEnemigos() {
        texturaEnemigos = new Texture("enemigos/minion.png");
    }

    public void procesarEstado(String mensaje) {

        if (mensaje == null) {
            return;
        }

        // ENEMIGOS|SECUENCIA|ID:TIPO:X:Y:VIDA|...
        String[] partes = mensaje.split("\\|", -1);

        if (partes.length < 2 || !partes[0].equals("ENEMIGOS")) {
            return;
        }

        try {
            long secuencia = Long.parseLong(partes[1]);

            if (secuencia <= ultimaSecuencia) {
                return;
            }

            List<DatosEnemigo> recibidos = new ArrayList<>();
            Set<Integer> idsRecibidos = new HashSet<>();

            // Validar todo el estado antes de aplicarlo.
            for (int i = 2; i < partes.length; i++) {

                String[] datos = partes[i].split(":", -1);

                if (datos.length != 5) {
                    return;
                }

                int id = Integer.parseInt(datos[0]);
                String tipo = datos[1];

                float x = Float.parseFloat(datos[2]);
                float y = Float.parseFloat(datos[3]);
                int vida = Integer.parseInt(datos[4]);

                if (id <= 0 || !tipo.equals("BASICO") || !Float.isFinite(x) || !Float.isFinite(y) || vida < 0 || !idsRecibidos.add(id)) {
                    return;
                }

                recibidos.add(new DatosEnemigo(id, x, y, vida));
            }

            // Crear o actualizar las representaciones visuales.
            for (DatosEnemigo datos : recibidos) {

                EnemigoBasico enemigo = enemigos.get(datos.id());

                if (enemigo == null) {
                    enemigo = new EnemigoBasico(25, 80f, datos.x(), datos.y(), texturaEnemigos);

                    enemigos.put(datos.id(), enemigo);
                }

                enemigo.setPosicion(datos.x(), datos.y());
                enemigo.setVida(datos.vida());
            }

            // Eliminar enemigos ausentes en el nuevo estado.
            List<Integer> idsActuales = new ArrayList<>(enemigos.keySet());

            for (int id : idsActuales) {

                if (!idsRecibidos.contains(id)) {

                    EnemigoBasico eliminado = enemigos.remove(id);

                    if (eliminado != null) {
                        eliminado.dispose();
                    }
                }
            }

            ultimaSecuencia = secuencia;

        } catch (NumberFormatException e) {
            Gdx.app.error("GestorEnemigosVisual", "Estado de enemigos invalido: " + mensaje);
        }
    }

    public void dibujar(Batch batch, float delta) {

        for (EnemigoBasico enemigo : enemigos.values()) {

            if (enemigo.estaVivo()) {
                enemigo.dibujar(batch, delta);
            }
        }
    }

    public void dispose() {

        for (EnemigoBasico enemigo : enemigos.values()) {
            enemigo.dispose();
        }

        enemigos.clear();
        texturaEnemigos.dispose();
    }

    private record DatosEnemigo(int id, float x, float y, int vida) {
    }
}
