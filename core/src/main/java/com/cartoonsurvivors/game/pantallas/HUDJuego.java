
package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.Pasivas;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.Resultado;

import java.util.Locale;
import java.util.Map;

import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;


public class HUDJuego {

    private static final Color PANEL = new Color(0x192036ef);
    static final Color TEXTO_HUD = new Color(0xfff2d6ff);
    private static final Color FONDO_BARRA = new Color(0x361d2cff);
    private static final Color EXPERIENCIA = new Color(0x268cecff);
    private static final Color VIDA = new Color(0xf35266ff);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ExtendViewport hudViewport;
    private final Texture pixel;
    private final TextureRegion flechaCompanero;
    private final Texture iconoEspada;
    private final Texture iconoPistola;
    private final Texture fondoHud;
    private final Texture fondoVictoria;
    private final Texture fondoDerrota;
    private final Texture marcoXp;
    private final Texture marcoElegido;
    private final Texture marcoNoElegido;
    private final Map<String, Texture> iconosMejora;
    private final GlyphLayout textoCentrado = new GlyphLayout();

    public HUDJuego(
        SpriteBatch batch,
        BitmapFont font,
        ExtendViewport hudViewport) {

        this.batch = batch;
        this.font = font;
        this.hudViewport = hudViewport;

        Pixmap imagen = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        imagen.setColor(Color.WHITE);
        imagen.fill();
        pixel = new Texture(imagen);
        imagen.dispose();
        flechaCompanero = new TextureRegion(crearFlechaCompanero());
        iconoEspada = cargarIcono("armas/marco-espada.png");
        iconoPistola = cargarIcono("armas/marco-pistola.png");
        fondoHud = cargarTextura("Hud/fondo-mejora.png");
        fondoVictoria = cargarTextura("resultados/victoria.png");
        fondoDerrota = cargarTextura("resultados/derrota.png");
        marcoXp = cargarTextura("Hud/barraXp.png");
        marcoElegido = cargarTextura("Hud/marco-elegido.png");
        marcoNoElegido = cargarTextura("Hud/marco-noElegido.png");
        iconosMejora = Map.of(
            "VELOCIDAD", cargarTextura("Hud/velocidad.png"),
            "VIDA_MAXIMA", cargarTextura("Hud/vida-maxima.png"),
            "REGENERACION", cargarTextura("Hud/regeneracion.png"),
            "ARMADURA", cargarTextura("Hud/armadura.png"),
            "CRITICO", cargarTextura("Hud/critico.png"));
    }

    // HUD utilizado durante la partida multijugador.
    public void dibujarMultijugador(
        Jugador jugadorLocal,
        Jugador jugadorRemoto,
        boolean estadoRecibido,
        EstadisticasPartida estadisticas,
        Pasivas pasivas) {

        prepararHUD();

        batch.begin();

        float arriba = hudViewport.getWorldHeight() - 20;
        float centro = hudViewport.getWorldWidth() / 2f;
        float derecha = hudViewport.getWorldWidth() - 20;

        dibujarContadorBajas(estadisticas, arriba);

        int segundos = estadisticas == null ? 0 : estadisticas.segundos();
        String tiempo = estadisticas == null ? "--:--"
            : String.format(Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60);
        batch.draw(fondoHud, centro - 92, arriba - 58, 184, 58);
        textoCentrado(tiempo, centro, arriba - 13, TEXTO_HUD);

        float izquierdaXp = derecha - 288;
        batch.draw(fondoHud, izquierdaXp, arriba - 94, 288, 94);
        batch.draw(marcoElegido, derecha - 86, arriba - 86, 78, 78);
        dibujarIcono(jugadorLocal.getTexturaIdle(), derecha - 47, arriba - 47, 48);

        int nivel = estadisticas == null ? 1 : estadisticas.nivel();
        int experiencia = estadisticas == null ? 0 : estadisticas.experiencia();
        int meta = estadisticas == null ? 0 : estadisticas.meta();
        float proporcionXp = meta > 0 ? MathUtils.clamp((float) experiencia / meta, 0f, 1f) : 0f;
        dibujarExperiencia(proporcionXp, izquierdaXp + 12, arriba - 64, 188);
        float escalaX = font.getScaleX();
        float escalaY = font.getScaleY();
        font.getData().setScale(escalaX * 0.55f, escalaY * 0.55f);
        textoCentrado("Nivel " + nivel, izquierdaXp + 106, arriba - 17, TEXTO_HUD);
        textoCentrado(meta > 0 ? experiencia + " / " + meta + " XP" : "-- XP",
            izquierdaXp + 106, arriba - 66, TEXTO_HUD);
        dibujarArmas(jugadorLocal, izquierdaXp + 12, arriba - 164);
        if (pasivas != null) {
            font.getData().setScale(escalaX * 0.43f, escalaY * 0.43f);
            dibujarPasivas(pasivas, izquierdaXp, arriba - 174);
            font.getData().setScale(escalaX * 0.55f, escalaY * 0.55f);
        }

        if (!estadoRecibido) {
            textoCentrado("Esperando al servidor...", centro, arriba - 76, TEXTO_HUD);
        } else if (estadisticas == null || (estadisticas.rondaMejora() == null
                && estadisticas.resultado() == Resultado.EN_CURSO)) {
            dibujarReaparicion(jugadorLocal, jugadorRemoto, centro, arriba);
        }

        font.getData().setScale(escalaX, escalaY);
        batch.end();
        font.setColor(Color.WHITE);
    }

    private void dibujarContadorBajas(EstadisticasPartida estadisticas, float arriba) {
        float ancho = 312f;
        float izquierda = 20f;
        batch.draw(fondoHud, izquierda, arriba - 58f, ancho, 58f);
        String texto = "Kills " + (estadisticas == null ? "--" : estadisticas.kills());
        font.setColor(TEXTO_HUD);
        textoCentrado.setText(font, texto);
        float escalaX = font.getScaleX();
        float escalaY = font.getScaleY();
        float ajuste = Math.min(1f, (ancho - 36f) / textoCentrado.width);
        font.getData().setScale(escalaX * ajuste, escalaY * ajuste);
        textoCentrado(texto, izquierda + ancho / 2f, arriba - 29f + font.getCapHeight() / 2f, TEXTO_HUD);
        font.getData().setScale(escalaX, escalaY);
    }

    void dibujarReaparicion(Jugador local, Jugador remoto, float centro, float arriba) {
        boolean reapareceLocal = local.getVida() <= 0f;
        boolean reapareceRemoto = remoto.getVida() <= 0f;
        if (!reapareceLocal && !reapareceRemoto) {
            return;
        }

        float alto = reapareceLocal && reapareceRemoto ? 68 : 48;
        batch.draw(fondoHud, centro - 238, arriba - 66 - alto, 476, alto);
        float y = arriba - 80;
        if (reapareceLocal) {
            textoCentrado(mensajeReaparicion("Reaparecés", local), centro, y, TEXTO_HUD);
            y -= 22;
        }
        if (reapareceRemoto) {
            textoCentrado(mensajeReaparicion("Tu compañero reaparece", remoto), centro, y, TEXTO_HUD);
        }
    }

    private String mensajeReaparicion(String texto, Jugador jugador) {
        int segundos = jugador.getSegundosParaReaparecer();
        return segundos > 0 ? texto + " en " + segundos + " s" : "Esperando reaparición...";
    }

    private Texture cargarIcono(String ruta) {
        var archivo = Gdx.files.internal(ruta);
        return archivo.exists() ? cargarTextura(ruta) : null;
    }

    static Texture cargarTextura(String ruta) {
        Texture textura = new Texture(Gdx.files.internal(ruta));
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return textura;
    }

    private void dibujarExperiencia(float proporcion, float x, float y, float ancho) {
        float alto = ancho * marcoXp.getHeight() / marcoXp.getWidth();
        // El hueco del marco ocupa los píxeles 32–636 en horizontal y 28–64 en vertical.
        float unidad = ancho / marcoXp.getWidth();
        float inicio = x + 32 * unidad;
        float base = y + alto - 64 * unidad;
        float anchoInterior = 604 * unidad;
        rectangulo(FONDO_BARRA, inicio, base, anchoInterior, 36 * unidad);
        if (proporcion > 0f) {
            rectangulo(EXPERIENCIA, inicio, base, anchoInterior * proporcion, 36 * unidad);
        }
        batch.draw(marcoXp, x, y, ancho, alto);
    }

    void dibujarIcono(Texture icono, float centroX, float centroY, float tamanio) {
        float escala = tamanio / Math.max(icono.getWidth(), icono.getHeight());
        float ancho = icono.getWidth() * escala;
        float alto = icono.getHeight() * escala;
        batch.draw(icono, centroX - ancho / 2f, centroY - alto / 2f, ancho, alto);
    }

    private void dibujarArmas(Jugador jugador, float x, float y) {
        for (Arma arma : jugador.getArmas()) {
            boolean espada = arma instanceof Espada;
            Texture icono = espada ? iconoEspada : iconoPistola;
            if (icono == null) {
                batch.draw(marcoNoElegido, x, y, 64, 64);
                textoCentrado(espada ? "Espada" : "Pistola", x + 32, y + 42, TEXTO_HUD);
            } else {
                batch.draw(icono, x, y, 64, 64);
            }
            x += 68;
        }
    }

    private void dibujarPasivas(Pasivas pasivas, float x, float arriba) {
        batch.draw(fondoHud, x, arriba - 76f, 288f, 76f);
        float izquierda = x + 12f;
        float primeraFila = arriba - 19f;
        float segundaFila = arriba - 54f;
        dibujarPasiva("CRITICO", pasivas.critico() + " %", izquierda, primeraFila);
        dibujarPasiva("ARMADURA", pasivas.armadura() + " %", izquierda + 88f, primeraFila);
        dibujarPasiva("VELOCIDAD", valorPasiva(pasivas.velocidad()), izquierda + 176f, primeraFila);
        dibujarPasiva("VIDA_MAXIMA", valorPasiva(pasivas.vidaMaxima()), izquierda, segundaFila);
        dibujarPasiva("REGENERACION", valorPasiva(pasivas.regeneracion()) + "/s", izquierda + 88f, segundaFila);
    }

    private static Texture crearFlechaCompanero() {
        Pixmap imagen = new Pixmap(36, 32, Pixmap.Format.RGBA8888);
        imagen.setColor(0x171d32ff);
        imagen.fillRectangle(2, 10, 18, 12);
        imagen.fillTriangle(16, 1, 34, 16, 16, 31);
        imagen.setColor(0x81e6ffff);
        imagen.fillRectangle(5, 13, 16, 6);
        imagen.fillTriangle(19, 7, 30, 16, 19, 25);
        Texture textura = new Texture(imagen);
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        imagen.dispose();
        return textura;
    }

    public void dibujarFlechaCompanero(float dx, float dy) {
        float centroX = hudViewport.getWorldWidth() / 2f;
        float centroY = hudViewport.getWorldHeight() / 2f;
        float proporcion = Math.min((centroX - 24f) / Math.abs(dx), (centroY - 24f) / Math.abs(dy));
        float x = centroX + dx * proporcion;
        float y = centroY + dy * proporcion;
        float angulo = (float) Math.toDegrees(Math.atan2(dy, dx));
        prepararHUD();
        batch.begin();
        batch.draw(flechaCompanero, x - 18f, y - 16f, 18f, 16f, 36f, 32f, 1f, 1f, angulo);
        batch.end();
    }

    private void dibujarPasiva(String tipo, String valor, float x, float centroY) {
        dibujarIcono(iconosMejora.get(tipo), x + 14f, centroY, 28f);
        font.setColor(TEXTO_HUD);
        font.draw(batch, valor, x + 31f, centroY + font.getCapHeight() / 2f);
    }

    private String valorPasiva(float valor) {
        return valor == Math.rint(valor)
            ? String.format(Locale.ROOT, "%.0f", valor)
            : String.format(Locale.ROOT, "%.1f", valor);
    }

    void dibujarFondo(float x, float y, float ancho, float alto) {
        batch.draw(fondoHud, x, y, ancho, alto);
    }

    void dibujarMarcoOpcion(boolean seleccionada, float x, float y, float tamano) {
        batch.draw(seleccionada ? marcoElegido : marcoNoElegido, x, y, tamano, tamano);
    }

    Texture iconoPasiva(String tipo) {
        return iconosMejora.get(tipo);
    }

    public void dibujarBarraJugador(Jugador jugador) {
        if (jugador.getVida() <= 0f) {
            return;
        }
        float ancho = 58;
        float centro = jugador.getPosicionX() + jugador.getHitbox().width / 2f;
        float pieDelSprite = jugador.getPosicionY()
            - (TAMAÑO_REAL - jugador.getHitbox().height) / 2f;
        dibujarBarra(jugador, centro - ancho / 2f, pieDelSprite - 9, ancho, 7);
    }

    private void dibujarBarra(Jugador jugador, float x, float y, float ancho, float alto) {
        float proporcion = MathUtils.clamp(jugador.getVida() / jugador.getVidaMaxima(), 0f, 1f);
        dibujarBarra(proporcion, VIDA, x, y, ancho, alto);
    }

    private void dibujarBarra(float proporcion, Color color, float x, float y, float ancho, float alto) {
        rectangulo(PANEL, x, y, ancho, alto);
        rectangulo(FONDO_BARRA, x + 2, y + 2, ancho - 4, alto - 4);
        if (proporcion > 0f) {
            rectangulo(color, x + 2, y + 2, (ancho - 4) * proporcion, alto - 4);
        }
    }

    void rectangulo(Color color, float x, float y, float ancho, float alto) {
        batch.setColor(color);
        batch.draw(pixel, x, y, ancho, alto);
        batch.setColor(Color.WHITE);
    }

    void textoCentrado(String texto, float centro, float y, Color color) {
        font.setColor(color);
        textoCentrado.setText(font, texto);
        font.draw(batch, textoCentrado, centro - textoCentrado.width / 2f, y);
    }

    public void dispose() {
        pixel.dispose();
        flechaCompanero.getTexture().dispose();
        fondoHud.dispose();
        fondoVictoria.dispose();
        fondoDerrota.dispose();
        marcoXp.dispose();
        marcoElegido.dispose();
        marcoNoElegido.dispose();
        iconosMejora.values().forEach(Texture::dispose);
        if (iconoEspada != null) {
            iconoEspada.dispose();
        }
        if (iconoPistola != null) {
            iconoPistola.dispose();
        }
    }

    // Aviso mostrado al finalizar la sesion multijugador.
    public void dibujarSesionFinalizada(
        boolean servidorDesconectado) {

        prepararHUD();

        batch.begin();

        font.setColor(Color.RED);

        String mensaje = servidorDesconectado
            ? "SERVIDOR DESCONECTADO"
            : "SESION FINALIZADA";

        font.draw(batch, mensaje, 350, 400);

        font.setColor(Color.WHITE);

        font.draw(batch,
            "Presiona ESC para volver al menu",
            310, 320);

        batch.end();
    }

    void prepararHUD() {
        hudViewport.apply();
        batch.setProjectionMatrix(
            hudViewport.getCamera().combined
        );
    }

    public void dibujarResultado(Resultado resultado, int enemigosEliminados) {
        prepararHUD();
        Texture fondo = resultado == Resultado.VICTORIA ? fondoVictoria : fondoDerrota;
        float ancho = hudViewport.getWorldWidth();
        float alto = hudViewport.getWorldHeight();
        float centroX = ancho / 2f;
        float centroY = alto / 2f;
        float escala = Math.min(ancho / fondo.getWidth(), alto / fondo.getHeight());
        float anchoFondo = fondo.getWidth() * escala;
        float altoFondo = fondo.getHeight() * escala;
        batch.begin();
        batch.draw(fondo, centroX - anchoFondo / 2f, centroY - altoFondo / 2f, anchoFondo, altoFondo);
        float linea = font.getLineHeight();
        float inicioTexto = centroY + (linea + font.getCapHeight()) / 2f;
        textoCentrado("Enemigos eliminados: " + enemigosEliminados, centroX, inicioTexto, Color.WHITE);
        textoCentrado("Presiona ESC para volver al menú", centroX, inicioTexto - linea, Color.WHITE);
        batch.end();
    }

}
