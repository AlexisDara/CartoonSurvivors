package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.cartoonsurvivors.game.entidades.enemigos.Enemigo;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.enemigos.Jefe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestorEnemigos {

    private final Map<Integer, Enemigo> enemigos = new HashMap<>();

    private final Texture texturaEnemigos;
    private final Texture texturaJefe;
    private final Texture texturaFuego;
    private List<CirculoFuego> circulosFuego = List.of();

    private long ultimaSecuencia = -1;

    public GestorEnemigos() {
        texturaEnemigos = new Texture("enemigos/minion.png");
        texturaJefe = new Texture("enemigos/elmacho.png");
        texturaFuego = crearTexturaFuego();
    }

    public void procesarEstado(String mensaje) {

        if (mensaje == null) {
            return;
        }

        // ENEMIGOS|SECUENCIA|ID:TIPO:X:Y:VIDA|FUEGO:X:Y:RADIO:OPACIDAD|...
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
            List<CirculoFuego> fuegoRecibido = new ArrayList<>();
            Set<Integer> idsRecibidos = new HashSet<>();

            // Validar todo el estado antes de aplicarlo.
            for (int i = 2; i < partes.length; i++) {

                String[] datos = partes[i].split(":", -1);

                if (datos.length != 5) {
                    return;
                }

                if (datos[0].equals("FUEGO")) {
                    float x = Float.parseFloat(datos[1]);
                    float y = Float.parseFloat(datos[2]);
                    float radio = Float.parseFloat(datos[3]);
                    float opacidad = Float.parseFloat(datos[4]);
                    if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(radio) || radio <= 0f
                            || !Float.isFinite(opacidad) || opacidad < 0f || opacidad > 1f) {
                        return;
                    }
                    fuegoRecibido.add(new CirculoFuego(x, y, radio, opacidad));
                    continue;
                }

                int id = Integer.parseInt(datos[0]);
                String tipo = datos[1];

                float x = Float.parseFloat(datos[2]);
                float y = Float.parseFloat(datos[3]);
                int vida = Integer.parseInt(datos[4]);

                if (id <= 0 || (!tipo.equals("BASICO") && !tipo.equals("JEFE")) || !Float.isFinite(x) || !Float.isFinite(y) || vida < 0 || !idsRecibidos.add(id)) {
                    return;
                }

                recibidos.add(new DatosEnemigo(id, tipo, x, y, vida));
            }

            // Crear o actualizar las representaciones visuales.
            for (DatosEnemigo datos : recibidos) {

                Enemigo enemigo = enemigos.get(datos.id());

                if (enemigo == null) {
                    enemigo = datos.tipo().equals("JEFE")
                        ? new Jefe(datos.vida(), datos.x(), datos.y(), texturaJefe)
                        : new EnemigoBasico(datos.vida(), 80f, datos.x(), datos.y(), texturaEnemigos);

                    enemigos.put(datos.id(), enemigo);
                }

                enemigo.setPosicion(datos.x(), datos.y());
                enemigo.setVida(datos.vida());
            }

            // Eliminar enemigos ausentes en el nuevo estado.
            List<Integer> idsActuales = new ArrayList<>(enemigos.keySet());

            for (int id : idsActuales) {

                if (!idsRecibidos.contains(id)) {

                    Enemigo eliminado = enemigos.remove(id);

                    if (eliminado != null) {
                        eliminado.dispose();
                    }
                }
            }

            circulosFuego = fuegoRecibido;
            ultimaSecuencia = secuencia;

        } catch (NumberFormatException e) {
            Gdx.app.error("GestorEnemigosVisual", "Estado de enemigos invalido: " + mensaje);
        }
    }

    public void dibujarFuego(Batch batch) {
        if (circulosFuego.isEmpty()) {
            return;
        }
        float colorAnterior = batch.getPackedColor();
        for (CirculoFuego circulo : circulosFuego) {
            float diametro = circulo.radio() * 2f;
            batch.setColor(1f, 1f, 1f, circulo.opacidad());
            batch.draw(texturaFuego, circulo.x() - circulo.radio(), circulo.y() - circulo.radio(),
                diametro, diametro);
        }
        batch.setPackedColor(colorAnterior);
    }

    private static Texture crearTexturaFuego() {
        int tamano = 64;
        float centro = (tamano - 1) / 2f;
        Pixmap pixmap = new Pixmap(tamano, tamano, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < tamano; y++) {
            for (int x = 0; x < tamano; x++) {
                float distancia = (float) Math.hypot(x - centro, y - centro) / centro;
                float brillo = Math.max(0f, 1f - distancia);
                float opacidad = Math.min(1f, brillo * 8f);
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 0.19f + 0.76f * brillo,
                    0.04f + 0.24f * brillo, opacidad));
            }
        }
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return textura;
    }

    public void dibujar(Batch batch, float delta) {

        for (Enemigo enemigo : enemigos.values()) {

            if (enemigo.estaVivo()) {
                enemigo.dibujar(batch, delta);
            }
        }
    }

    public void dispose() {

        for (Enemigo enemigo : enemigos.values()) {
            enemigo.dispose();
        }

        enemigos.clear();
        texturaEnemigos.dispose();
        texturaJefe.dispose();
        texturaFuego.dispose();
    }

    private record CirculoFuego(float x, float y, float radio, float opacidad) {
    }

    private record DatosEnemigo(int id, String tipo, float x, float y, int vida) {
    }
}
