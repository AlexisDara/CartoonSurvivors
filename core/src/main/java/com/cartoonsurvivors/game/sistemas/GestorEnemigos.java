package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cartoonsurvivors.game.entidades.enemigos.Enemigo;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.enemigos.Jefe;
import com.cartoonsurvivors.game.entidades.enemigos.Muerte;
import com.cartoonsurvivors.game.utilidades.TipoMapa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestorEnemigos {

    private static final Set<String> TIPOS_ENEMIGOS = Set.of("BASICO", "VIOLETA", "JEFE", "MUERTE");

    private final Map<Integer, Enemigo> enemigos = new HashMap<>();

    private final boolean esMapa2;
    private final Texture texturaEnemigos;
    private final Texture texturaVioleta;
    private final Texture texturaJefe;
    private final Texture texturaMuerte;
    private final Texture texturaFuego;
    private final Texture pixelBarraVida;
    private final TextureRegion texturaBala;
    private List<CirculoFuego> circulosFuego = List.of();
    private List<Bala> balas = List.of();

    private long ultimaSecuencia = -1;

    public GestorEnemigos(TipoMapa mapa) {
        esMapa2 = mapa == TipoMapa.MAPA2;
        texturaEnemigos = new Texture(esMapa2 ? "enemigos/marciano.png" : "enemigos/minion.png");
        texturaVioleta = new Texture(esMapa2 ? "enemigos/woody.png" : "enemigos/minionvioleta.png");
        texturaJefe = new Texture(esMapa2 ? "enemigos/rex.png" : "enemigos/elmacho.png");
        texturaMuerte = new Texture("enemigos/Muerte.png");
        texturaFuego = crearTexturaFuego();
        texturaBala = new TextureRegion(crearTexturaBala());
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        pixelBarraVida = new Texture(pixel);
        pixel.dispose();
    }

    public void procesarEstado(String mensaje) {

        if (mensaje == null) {
            return;
        }

        // ENEMIGOS|SECUENCIA|ID:TIPO:X:Y:VIDA[:VIDA_MAXIMA]|FUEGO:X:Y:RADIO:OPACIDAD|BALA:X:Y:ANGULO|...
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
            List<Bala> balasRecibidas = new ArrayList<>();
            Set<Integer> idsRecibidos = new HashSet<>();

            // Validar todo el estado antes de aplicarlo.
            for (int i = 2; i < partes.length; i++) {

                String[] datos = partes[i].split(":", -1);

                if (datos[0].equals("BALA")) {
                    if (datos.length != 4) {
                        return;
                    }
                    float x = Float.parseFloat(datos[1]);
                    float y = Float.parseFloat(datos[2]);
                    float angulo = Float.parseFloat(datos[3]);
                    if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(angulo)) {
                        return;
                    }
                    balasRecibidas.add(new Bala(x, y, angulo));
                    continue;
                }

                if (datos.length != 5 && datos.length != 6) {
                    return;
                }

                if (datos[0].equals("FUEGO")) {
                    if (datos.length != 5) {
                        return;
                    }
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
                int vidaMaxima = 0;
                if (datos.length == 6) {
                    vidaMaxima = Integer.parseInt(datos[5]);
                    if (!tipo.equals("JEFE") || vidaMaxima <= 0 || vida > vidaMaxima) {
                        return;
                    }
                }

                if (id <= 0 || !TIPOS_ENEMIGOS.contains(tipo) || !Float.isFinite(x) || !Float.isFinite(y)
                        || vida < 0 || !idsRecibidos.add(id)) {
                    return;
                }

                recibidos.add(new DatosEnemigo(id, tipo, x, y, vida, vidaMaxima));
            }

            // Crear o actualizar las representaciones visuales.
            for (DatosEnemigo datos : recibidos) {

                Enemigo enemigo = enemigos.get(datos.id());

                if (enemigo == null) {
                    enemigo = switch (datos.tipo()) {
                        case "JEFE" -> new Jefe(datos.vida(), datos.x(), datos.y(), texturaJefe, esMapa2 ? 64 : 128);
                        case "MUERTE" -> new Muerte(datos.vida(), datos.x(), datos.y(), texturaMuerte);
                        case "VIOLETA" -> new EnemigoBasico(datos.vida(), 225f, datos.x(), datos.y(), texturaVioleta, esMapa2 ? 64 : 48);
                        default -> new EnemigoBasico(datos.vida(), 80f, datos.x(), datos.y(), texturaEnemigos, 48);
                    };

                    enemigos.put(datos.id(), enemigo);
                }

                enemigo.setPosicion(datos.x(), datos.y());
                enemigo.setVida(datos.vida());
                if (enemigo instanceof Jefe jefe) {
                    jefe.setVidaMaxima(datos.vidaMaxima());
                }
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
            balas = balasRecibidas;
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

    private static Texture crearTexturaBala() {
        Pixmap pixmap = new Pixmap(16, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(0x4a2925ff);
        pixmap.fillRectangle(4, 0, 8, 8);
        pixmap.fillCircle(4, 4, 3);
        pixmap.fillCircle(12, 4, 3);
        pixmap.setColor(0xffb94aff);
        pixmap.fillRectangle(4, 2, 8, 4);
        pixmap.fillCircle(12, 4, 2);
        pixmap.setColor(0xffead0ff);
        pixmap.drawLine(6, 2, 12, 2);
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    public void dibujarBalas(Batch batch) {
        for (Bala bala : balas) {
            batch.draw(texturaBala, bala.x() - 12f, bala.y() - 6f, 12f, 6f, 24f, 12f, 1f, 1f, bala.angulo());
        }
    }

    public void dibujar(Batch batch, float delta) {

        for (Enemigo enemigo : enemigos.values()) {

            if (enemigo.estaVivo()) {
                enemigo.dibujar(batch, delta);
            }
        }
        for (Enemigo enemigo : enemigos.values()) {
            if (enemigo instanceof Jefe jefe && jefe.estaVivo()) {
                jefe.dibujarBarraVida(batch, pixelBarraVida);
            }
        }
    }

    public void dispose() {

        for (Enemigo enemigo : enemigos.values()) {
            enemigo.dispose();
        }

        enemigos.clear();
        texturaEnemigos.dispose();
        texturaVioleta.dispose();
        texturaJefe.dispose();
        texturaMuerte.dispose();
        texturaFuego.dispose();
        texturaBala.getTexture().dispose();
        pixelBarraVida.dispose();
    }

    private record CirculoFuego(float x, float y, float radio, float opacidad) {
    }

    private record Bala(float x, float y, float angulo) {
    }

    private record DatosEnemigo(int id, String tipo, float x, float y, int vida, int vidaMaxima) {
    }
}
