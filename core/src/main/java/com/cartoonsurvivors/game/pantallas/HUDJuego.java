
package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;

import java.util.Locale;

import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.VIDA_INICIAL;
import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_MUNDO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_MUNDO;

public class HUDJuego {

    private static final int ANCHO_MENU = 620;
    private static final int ALTO_OPCION = 56;
    private static final int PASO_OPCION = 68;
    private static final Color CAPA_PAUSA = new Color(0x080d19bd);
    private static final Color FONDO_OPCION = new Color(0x2a3655ff);
    private static final Color PANEL = new Color(0x192036ef);
    private static final Color MARCO = new Color(0xffd36aff);
    private static final Color FONDO_BARRA = new Color(0x361d2cff);
    private static final Color EXPERIENCIA = new Color(0x65d7faff);
    private static final Color VIDA = new Color(0xf35266ff);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ExtendViewport hudViewport;
    private final Texture pixel;
    private final Vector2 punteroHud = new Vector2();
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
    }

    // HUD utilizado durante la partida multijugador.
    public void dibujarMultijugador(
        Jugador jugadorLocal,
        Jugador jugadorRemoto,
        boolean estadoRecibido,
        EstadisticasPartida estadisticas) {

        prepararHUD();

        batch.begin();

        float arriba = hudViewport.getWorldHeight() - 20;
        float centro = hudViewport.getWorldWidth() / 2f;
        float derecha = hudViewport.getWorldWidth() - 20;

        rectangulo(PANEL, 20, arriba - 58, 216, 58);
        textoCentrado("Kills " + (estadisticas == null ? "--" : estadisticas.kills()),
            128, arriba - 9, Color.WHITE);

        int segundos = estadisticas == null ? 0 : estadisticas.segundos();
        String tiempo = estadisticas == null ? "--:--"
            : String.format(Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60);
        rectangulo(PANEL, centro - 92, arriba - 58, 184, 58);
        textoCentrado(tiempo, centro, arriba - 9, MARCO);

        float izquierdaVida = derecha - 280;
        rectangulo(PANEL, izquierdaVida, arriba - 132, 280, 132);
        rectangulo(MARCO, derecha - 72, arriba - 72, 64, 64);
        rectangulo(PANEL, derecha - 69, arriba - 69, 58, 58);
        batch.draw(jugadorLocal.getTexturaIdle(), derecha - 69, arriba - 69, 58, 58);
        textoCentrado((int) Math.ceil(jugadorLocal.getVida()) + " / " + VIDA_INICIAL,
            izquierdaVida + 104, arriba - 8, Color.WHITE);
        dibujarBarra(jugadorLocal, izquierdaVida + 12, arriba - 67, 184, 17);

        int nivel = estadisticas == null ? 1 : estadisticas.nivel();
        int experiencia = estadisticas == null ? 0 : estadisticas.experiencia();
        int meta = estadisticas == null ? 0 : estadisticas.meta();
        float proporcionXp = meta > 0 ? Math.min(1f, (float) experiencia / meta) : 0f;
        dibujarBarra(proporcionXp, EXPERIENCIA, izquierdaVida + 12, arriba - 89, 256, 14);
        float escalaX = font.getScaleX();
        float escalaY = font.getScaleY();
        font.getData().setScale(escalaX * 0.55f, escalaY * 0.55f);
        textoCentrado("Nivel " + nivel + "   ·   " + (meta > 0 ? experiencia + " / " + meta + " XP" : "-- XP"),
            izquierdaVida + 140, arriba - 99, EXPERIENCIA);

        font.getData().setScale(escalaX, escalaY);

        if (!estadoRecibido) {
            textoCentrado("Esperando al servidor...", centro, arriba - 76, Color.WHITE);
        } else if (jugadorRemoto.getVida() <= 0f) {
            rectangulo(PANEL, centro - 220, arriba - 120, 440, 50);
            textoCentrado("Tu compañero cayó", centro, arriba - 76, VIDA);
        }

        batch.end();
        font.setColor(Color.WHITE);
    }

    public void dibujarMejoras(EstadisticasPartida estadisticas, int seleccion, boolean confirmando) {
        prepararHUD();
        batch.begin();

        float centro = hudViewport.getWorldWidth() / 2f;
        float arriba = arribaMenuMejoras();
        float izquierda = izquierdaMenuMejoras();
        var ronda = estadisticas.rondaMejora();
        int segundos = (ronda.milisegundos() + 999) / 1000;

        rectangulo(CAPA_PAUSA, 0, 0, hudViewport.getWorldWidth(), hudViewport.getWorldHeight());
        rectangulo(PANEL, izquierda - 24, arriba - 388, ANCHO_MENU + 48, 388);
        textoCentrado("Nivel " + estadisticas.nivel(), centro, arriba - 18, MARCO);

        float escalaX = font.getScaleX();
        float escalaY = font.getScaleY();
        font.getData().setScale(escalaX * 0.65f, escalaY * 0.65f);
        textoCentrado("La partida continúa en " + segundos + " s", centro, arriba - 72, EXPERIENCIA);
        for (int i = 0; i < ronda.opciones().size(); i++) {
            float y = baseOpcion(i);
            boolean elegida = i == seleccion;
            rectangulo(elegida ? MARCO : FONDO_OPCION, izquierda, y, ANCHO_MENU, ALTO_OPCION);
            rectangulo(FONDO_OPCION, izquierda + 2, y + 2, ANCHO_MENU - 4, ALTO_OPCION - 4);
            font.setColor(elegida ? MARCO : Color.WHITE);
            font.draw(batch, (i + 1) + "   " + etiquetaMejora(ronda.opciones().get(i)),
                izquierda + 20, y + 39);
            if (elegida) {
                String estado = confirmando ? "Enviando…" : "Elegida";
                textoCentrado(estado, izquierda + ANCHO_MENU - 78, y + 39, MARCO);
            }
        }

        textoCentrado("Podés cambiar tu elección con clic o con 1–3", centro, arriba - 331, Color.WHITE);
        font.getData().setScale(escalaX, escalaY);
        font.setColor(Color.WHITE);
        batch.end();
    }

    int obtenerOpcionMejora(int x, int y) {
        hudViewport.unproject(punteroHud.set(x, y));
        float izquierda = izquierdaMenuMejoras();
        if (punteroHud.x < izquierda || punteroHud.x > izquierda + ANCHO_MENU) {
            return -1;
        }
        for (int i = 0; i < 3; i++) {
            float base = baseOpcion(i);
            if (punteroHud.y >= base && punteroHud.y <= base + ALTO_OPCION) {
                return i;
            }
        }
        return -1;
    }

    private float izquierdaMenuMejoras() {
        return (hudViewport.getWorldWidth() - ANCHO_MENU) / 2f;
    }

    private float arribaMenuMejoras() {
        return hudViewport.getWorldHeight() / 2f + 194;
    }

    private float baseOpcion(int indice) {
        return arribaMenuMejoras() - 164 - indice * PASO_OPCION;
    }

    private String etiquetaMejora(String mejora) {
        return switch (mejora) {
            case "DANIO" -> "Daño de espada +5";
            case "ALCANCE" -> "Alcance de espada +15 %";
            case "CADENCIA" -> "Intervalo de ataque -10 %";
            case "VELOCIDAD" -> "Velocidad de movimiento +20";
            default -> mejora;
        };
    }

    public void dibujarBarraJugador(Jugador jugador) {
        float ancho = 58;
        float centro = jugador.getPosicionX() + jugador.getHitbox().width / 2f;
        float pieDelSprite = jugador.getPosicionY()
            - (TAMAÑO_REAL - jugador.getHitbox().height) / 2f;
        dibujarBarra(jugador, centro - ancho / 2f, pieDelSprite - 9, ancho, 7);
    }

    private void dibujarBarra(Jugador jugador, float x, float y, float ancho, float alto) {
        float proporcion = MathUtils.clamp(jugador.getVida() / VIDA_INICIAL, 0f, 1f);
        dibujarBarra(proporcion, VIDA, x, y, ancho, alto);
    }

    private void dibujarBarra(float proporcion, Color color, float x, float y, float ancho, float alto) {
        rectangulo(PANEL, x, y, ancho, alto);
        rectangulo(FONDO_BARRA, x + 2, y + 2, ancho - 4, alto - 4);
        if (proporcion > 0f) {
            rectangulo(color, x + 2, y + 2, (ancho - 4) * proporcion, alto - 4);
        }
    }

    private void rectangulo(Color color, float x, float y, float ancho, float alto) {
        batch.setColor(color);
        batch.draw(pixel, x, y, ancho, alto);
        batch.setColor(Color.WHITE);
    }

    private void textoCentrado(String texto, float centro, float y, Color color) {
        font.setColor(color);
        textoCentrado.setText(font, texto);
        font.draw(batch, textoCentrado, centro - textoCentrado.width / 2f, y);
    }

    public void dispose() {
        pixel.dispose();
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

    private void prepararHUD() {
        hudViewport.apply();
        batch.setProjectionMatrix(
            hudViewport.getCamera().combined
        );
    }

    // HUD anterior, conservado por compatibilidad.
    public void dibujarHUD(
        Jugador jugador,
        float tiempoJuego,
        int enemigosMatados) {

        prepararHUD();

        batch.begin();

        font.setColor(Color.WHITE);

        font.draw(batch,
            "Vida: " + String.format("%.0f", jugador.getVida()),
            10, ALTO_MUNDO - 10);

        font.draw(batch,
            "Tiempo: " + String.format("%.0f", tiempoJuego),
            (ANCHO_MUNDO / 2) - 90f,
            ALTO_MUNDO - 10);

        font.draw(batch,
            "Enemigos: " + enemigosMatados,
            10, ALTO_MUNDO - 35);

        batch.end();
    }

    // Pantalla anterior, conservada por compatibilidad.
    public void dibujarPantallaVictoriaDerrota(
        Color color,
        String texto,
        int enemigosMatados,
        float tiempoJuego) {

        prepararHUD();

        GlyphLayout layout = new GlyphLayout();

        batch.begin();

        font.setColor(color);
        layout.setText(font, texto);

        font.draw(batch, texto,
            ANCHO_MUNDO / 2f - layout.width / 2f,
            500);

        font.setColor(Color.WHITE);

        String textoEnemigos =
            "Enemigos eliminados: " + enemigosMatados;

        layout.setText(font, textoEnemigos);

        font.draw(batch, textoEnemigos,
            ANCHO_MUNDO / 2f - layout.width / 2f,
            430);

        String textoTiempo =
            "Tiempo sobrevivido: "
                + String.format("%.0f", tiempoJuego)
                + " segundos";

        layout.setText(font, textoTiempo);

        font.draw(batch, textoTiempo,
            ANCHO_MUNDO / 2f - layout.width / 2f,
            370);

        String textoReiniciar = "Toca R para reiniciar";

        layout.setText(font, textoReiniciar);

        font.draw(batch, textoReiniciar,
            ANCHO_MUNDO / 2f - layout.width / 2f,
            270);

        String textoMenu = "Toca M para volver al menú";

        layout.setText(font, textoMenu);

        font.draw(batch, textoMenu,
            ANCHO_MUNDO / 2f - layout.width / 2f,
            210);

        batch.end();
    }
}
