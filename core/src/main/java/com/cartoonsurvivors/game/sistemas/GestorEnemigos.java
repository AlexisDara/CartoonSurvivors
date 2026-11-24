package com.cartoonsurvivors.game.sistemas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cartoonsurvivors.game.entidades.enemigos.AparienciaEnemigo;
import com.cartoonsurvivors.game.entidades.enemigos.Enemigo;
import com.cartoonsurvivors.game.entidades.enemigos.EnemigoBasico;
import com.cartoonsurvivors.game.entidades.enemigos.Jefe;
import com.cartoonsurvivors.game.entidades.enemigos.Muerte;
import com.cartoonsurvivors.game.utilidades.TipoMapa;

import com.cartoonsurvivors.game.sistemas.EstadoEnemigos.Bala;
import com.cartoonsurvivors.game.sistemas.EstadoEnemigos.CirculoFuego;
import com.cartoonsurvivors.game.sistemas.EstadoEnemigos.DatosEnemigo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorEnemigos {

    private final Map<Integer, Enemigo> enemigos = new HashMap<>();

    private final AparienciaEnemigo aspectoBasico;
    private final AparienciaEnemigo aspectoVioleta;
    private final AparienciaEnemigo aspectoJefe;
    private final Texture texturaMuerte;
    private final Texture texturaFuego;
    private final Texture pixelBarraVida;
    private final TextureRegion texturaBala;
    private List<CirculoFuego> circulosFuego = List.of();
    private List<Bala> balas = List.of();

    private long ultimaSecuencia = -1;

    public GestorEnemigos(TipoMapa mapa) {
        if (mapa == TipoMapa.MAPA2) {
            aspectoBasico = new AparienciaEnemigo("enemigos/marciano.png", 48, "enemigos/minion.png", 48);
            aspectoVioleta = new AparienciaEnemigo("enemigos/woody.png", 64, "enemigos/minionvioleta.png", 48);
            aspectoJefe = new AparienciaEnemigo("enemigos/rex.png", 64, "enemigos/elmacho.png", 128);
        } else {
            aspectoBasico = new AparienciaEnemigo("enemigos/minion.png", 48);
            aspectoVioleta = new AparienciaEnemigo("enemigos/minionvioleta.png", 48);
            aspectoJefe = new AparienciaEnemigo("enemigos/elmacho.png", 128);
        }
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
        EstadoEnemigos estado = EstadoEnemigos.leer(mensaje, ultimaSecuencia);
        if (estado == null) {
            return;
        }

        for (DatosEnemigo datos : estado.enemigos()) {
            Enemigo enemigo = enemigos.computeIfAbsent(datos.id(), id -> crearEnemigo(datos));
            enemigo.setPosicion(datos.x(), datos.y());
            enemigo.setVida(datos.vida());
            if (enemigo instanceof Jefe jefe) {
                jefe.setVidaMaxima(datos.vidaMaxima());
            }
        }

        var iterador = enemigos.entrySet().iterator();
        while (iterador.hasNext()) {
            var entrada = iterador.next();
            if (!estado.ids().contains(entrada.getKey())) {
                entrada.getValue().dispose();
                iterador.remove();
            }
        }

        circulosFuego = estado.fuego();
        balas = estado.balas();
        ultimaSecuencia = estado.secuencia();
    }

    private Enemigo crearEnemigo(DatosEnemigo datos) {
        return switch (datos.tipo()) {
            case "JEFE" -> new Jefe(datos.vida(), datos.x(), datos.y(), aspectoJefe);
            case "MUERTE" -> new Muerte(datos.vida(), datos.x(), datos.y(), texturaMuerte);
            case "VIOLETA" -> new EnemigoBasico(datos.vida(), 225f, datos.x(), datos.y(), aspectoVioleta);
            default -> new EnemigoBasico(datos.vida(), 80f, datos.x(), datos.y(), aspectoBasico);
        };
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
        aspectoBasico.dispose();
        aspectoVioleta.dispose();
        aspectoJefe.dispose();
        texturaMuerte.dispose();
        texturaFuego.dispose();
        texturaBala.getTexture().dispose();
        pixelBarraVida.dispose();
    }

}
