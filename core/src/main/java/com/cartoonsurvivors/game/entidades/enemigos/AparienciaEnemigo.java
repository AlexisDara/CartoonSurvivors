package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

public final class AparienciaEnemigo implements Disposable {
    private final Texture textura;
    private final Fotograma[] fotogramas;

    public AparienciaEnemigo(String ruta, int anchoFotograma) {
        this(ruta, anchoFotograma, ruta, anchoFotograma);
    }

    public AparienciaEnemigo(String ruta, int anchoFotograma, String referencia, int anchoReferencia) {
        var archivo = Gdx.files.internal(ruta);
        textura = new Texture(archivo);
        int cantidad = (textura.getWidth() + anchoFotograma - 1) / anchoFotograma;
        fotogramas = new Fotograma[cantidad];
        if (ruta.equals(referencia)) {
            for (int i = 0; i < cantidad; i++) {
                int x = i * anchoFotograma;
                int ancho = Math.min(anchoFotograma, textura.getWidth() - x);
                fotogramas[i] = new Fotograma(new Area(x, 0, ancho, textura.getHeight()), 0f, 0f, 1f, 1f);
            }
            return;
        }

        Pixmap imagen = new Pixmap(archivo);
        Pixmap base = new Pixmap(Gdx.files.internal(referencia));
        try {
            for (int i = 0; i < cantidad; i++) {
                int xBase = i * anchoReferencia;
                Area origen = areaVisible(imagen, i * anchoFotograma, anchoFotograma);
                Area destino = areaVisible(base, xBase, anchoReferencia);
                fotogramas[i] = new Fotograma(origen,
                    (destino.x() - xBase) / (float) anchoReferencia, destino.y() / (float) base.getHeight(),
                    destino.ancho() / (float) anchoReferencia, destino.alto() / (float) base.getHeight());
            }
        } finally {
            base.dispose();
            imagen.dispose();
        }
    }

    private static Area areaVisible(Pixmap imagen, int inicioX, int ancho) {
        int finX = Math.min(inicioX + ancho, imagen.getWidth());
        int izquierda = finX;
        int derecha = inicioX - 1;
        int arriba = imagen.getHeight();
        int abajo = -1;
        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = inicioX; x < finX; x++) {
                if ((imagen.getPixel(x, y) & 0xff) != 0) {
                    izquierda = Math.min(izquierda, x);
                    derecha = Math.max(derecha, x);
                    arriba = Math.min(arriba, y);
                    abajo = Math.max(abajo, y);
                }
            }
        }
        if (derecha < izquierda) {
            return new Area(inicioX, 0, finX - inicioX, imagen.getHeight());
        }
        return new Area(izquierda, arriba, derecha - izquierda + 1, abajo - arriba + 1);
    }

    void dibujar(Batch batch, int indice, float x, float y, float ancho, float alto, boolean mirandoIzquierda) {
        Fotograma frame = fotogramas[indice % fotogramas.length];
        Area origen = frame.origen();
        float margenX = mirandoIzquierda ? 1f - frame.x() - frame.ancho() : frame.x();
        float margenY = 1f - frame.y() - frame.alto();
        batch.draw(textura, x + margenX * ancho, y + margenY * alto, frame.ancho() * ancho, frame.alto() * alto,
            origen.x(), origen.y(), origen.ancho(), origen.alto(), mirandoIzquierda, false);
    }

    @Override
    public void dispose() {
        textura.dispose();
    }

    private record Area(int x, int y, int ancho, int alto) {
    }

    private record Fotograma(Area origen, float x, float y, float ancho, float alto) {
    }
}
