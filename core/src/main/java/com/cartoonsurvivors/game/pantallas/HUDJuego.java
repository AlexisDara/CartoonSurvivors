
package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.armas.Arma;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.Resultado;

import java.util.Locale;
import java.util.Map;

import static com.cartoonsurvivors.game.utilidades.Constantes.Jugador.TAMAÑO_REAL;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_MUNDO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_MUNDO;

public class HUDJuego {

    private static final int ANCHO_MENU = 766;
    private static final int ALTO_MENU = 370;
    private static final int TAMANIO_OPCION = 220;
    private static final int PASO_OPCION = 230;
    private static final Color CAPA_PAUSA = new Color(0x080d19bd);
    private static final Color PANEL = new Color(0x192036ef);
    private static final Color TEXTO_HUD = new Color(0xfff2d6ff);
    private static final Color FONDO_BARRA = new Color(0x361d2cff);
    private static final Color EXPERIENCIA = new Color(0x268cecff);
    private static final Color VIDA = new Color(0xf35266ff);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ExtendViewport hudViewport;
    private final Texture pixel;
    private final Texture iconoEspada;
    private final Texture iconoPistola;
    private final Texture fondoHud;
    private final Texture marcoXp;
    private final Texture marcoElegido;
    private final Texture marcoNoElegido;
    private final Texture espadaMejora;
    private final Texture pistolaMejora;
    private final Map<String, Texture> iconosMejora;
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
        iconoEspada = cargarIcono("armas/marco-espada.png");
        iconoPistola = cargarIcono("armas/marco-pistola.png");
        fondoHud = cargarTextura("Hud/fondo-mejora.png");
        marcoXp = cargarTextura("Hud/barraXp.png");
        marcoElegido = cargarTextura("Hud/marco-elegido.png");
        marcoNoElegido = cargarTextura("Hud/marco-noElegido.png");
        espadaMejora = cargarTextura("armas/espada.png");
        pistolaMejora = cargarTextura("armas/pistola.png");
        iconosMejora = Map.of(
            "DANIO", cargarTextura("Hud/danio.png"),
            "ALCANCE", cargarTextura("Hud/alcance.png"),
            "CADENCIA", cargarTextura("Hud/cadencia.png"),
            "VELOCIDAD", cargarTextura("Hud/velocidad.png"),
            "VIDA_MAXIMA", cargarTextura("Hud/vida-maxima.png"),
            "REGENERACION", cargarTextura("Hud/regeneracion.png"));
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

        batch.draw(fondoHud, 20, arriba - 58, 216, 58);
        textoCentrado("Kills " + (estadisticas == null ? "--" : estadisticas.kills()),
            128, arriba - 13, TEXTO_HUD);

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

    private void dibujarReaparicion(Jugador local, Jugador remoto, float centro, float arriba) {
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

    private Texture cargarTextura(String ruta) {
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

    private void dibujarIcono(Texture icono, float centroX, float centroY, float tamanio) {
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

    public void dibujarMejoras(Jugador jugador, Jugador jugadorRemoto, EstadisticasPartida estadisticas,
                              int seleccion, boolean confirmando) {
        prepararHUD();
        batch.begin();

        float centro = hudViewport.getWorldWidth() / 2f;
        float arriba = arribaMenuMejoras();
        var ronda = estadisticas.rondaMejora();
        int segundos = (ronda.milisegundos() + 999) / 1000;

        rectangulo(CAPA_PAUSA, 0, 0, hudViewport.getWorldWidth(), hudViewport.getWorldHeight());
        batch.draw(fondoHud, centro - ANCHO_MENU / 2f, arriba - ALTO_MENU, ANCHO_MENU, ALTO_MENU);
        float escalaX = font.getScaleX();
        float escalaY = font.getScaleY();
        font.getData().setScale(escalaX * 0.8f, escalaY * 0.8f);
        textoCentrado("Nivel " + estadisticas.nivel(), centro, arriba - 38, TEXTO_HUD);
        font.getData().setScale(escalaX * 0.43f, escalaY * 0.43f);
        textoCentrado("Tiempo restante " + segundos + " s", centro, arriba - 70, TEXTO_HUD);
        int cantidad = ronda.opciones().size();
        float y = baseOpciones();
        for (int i = 0; i < cantidad; i++) {
            float x = izquierdaOpcion(i, cantidad);
            String mejora = ronda.opciones().get(i);
            batch.draw(i == seleccion ? marcoElegido : marcoNoElegido, x, y, TAMANIO_OPCION, TAMANIO_OPCION);
            float centroOpcion = x + TAMANIO_OPCION / 2f;
            textoCentrado(Integer.toString(i + 1), centroOpcion, y + 181, TEXTO_HUD);
            String atributo = atributoMejora(mejora);
            Texture icono = iconoMejora(atributo, jugador);
            if (icono != null) {
                dibujarIcono(icono, centroOpcion, y + 133, 64);
            }
            font.setColor(TEXTO_HUD);
            font.draw(batch, etiquetaMejora(mejora, atributo, jugador),
                centroOpcion - 76, y + 93, 152, Align.center, true);
        }

        String indicacion = seleccion < 0 ? "Elegí con clic o con 1–3"
            : confirmando ? "Confirmando tu elección…" : "Esperando a tu compañero";
        textoCentrado(indicacion, centro, arriba - 307, TEXTO_HUD);
        font.getData().setScale(escalaX * 0.55f, escalaY * 0.55f);
        dibujarReaparicion(jugador, jugadorRemoto, centro, hudViewport.getWorldHeight() - 20);
        font.getData().setScale(escalaX, escalaY);
        font.setColor(Color.WHITE);
        batch.end();
    }

    int obtenerOpcionMejora(int x, int y, int cantidad) {
        hudViewport.unproject(punteroHud.set(x, y));
        float base = baseOpciones();
        if (punteroHud.y < base || punteroHud.y > base + TAMANIO_OPCION) {
            return -1;
        }
        for (int i = 0; i < cantidad; i++) {
            float izquierda = izquierdaOpcion(i, cantidad);
            if (punteroHud.x >= izquierda && punteroHud.x <= izquierda + TAMANIO_OPCION) {
                return i;
            }
        }
        return -1;
    }

    private float izquierdaOpcion(int indice, int cantidad) {
        float ancho = TAMANIO_OPCION + (cantidad - 1) * PASO_OPCION;
        return (hudViewport.getWorldWidth() - ancho) / 2f + indice * PASO_OPCION;
    }

    private float arribaMenuMejoras() {
        return (hudViewport.getWorldHeight() + ALTO_MENU) / 2f;
    }

    private float baseOpciones() {
        return arribaMenuMejoras() - 299;
    }

    private Texture iconoMejora(String mejora, Jugador jugador) {
        if ("ARMA".equals(mejora)) {
            return desbloqueaPistola(jugador) ? pistolaMejora : espadaMejora;
        }
        return iconosMejora.get(mejora);
    }

    private boolean desbloqueaPistola(Jugador jugador) {
        return jugador.getArmas().getFirst() instanceof Espada;
    }

    private String atributoMejora(String mejora) {
        if (mejora.endsWith("_ESPADA") || mejora.endsWith("_PISTOLA")) {
            return mejora.substring(0, mejora.lastIndexOf('_'));
        }
        return mejora;
    }

    private String etiquetaMejora(String mejora, String atributo, Jugador jugador) {
        String arma = mejora.endsWith("_ESPADA") ? "espada" : mejora.endsWith("_PISTOLA") ? "pistola" : "armas";
        return switch (atributo) {
            case "DANIO" -> "Daño de " + arma + " +5";
            case "ALCANCE" -> "Alcance de " + arma + " +15 %";
            case "CADENCIA" -> "Intervalo de " + arma + " -10 %";
            case "VELOCIDAD" -> "Velocidad de movimiento +20";
            case "ARMA" -> desbloqueaPistola(jugador) ? "Desbloquear pistola" : "Desbloquear espada";
            case "VIDA_MAXIMA" -> "Vida máxima +20 y curación +30";
            case "REGENERACION" -> "Regeneración +1 vida por segundo";
            default -> mejora;
        };
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
        fondoHud.dispose();
        marcoXp.dispose();
        marcoElegido.dispose();
        marcoNoElegido.dispose();
        espadaMejora.dispose();
        pistolaMejora.dispose();
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

    public void dibujarResultado(Resultado resultado, int enemigosEliminados) {
        prepararHUD();
        batch.begin();
        float centro = hudViewport.getWorldWidth() / 2f;
        float altura = hudViewport.getWorldHeight() / 2f;
        boolean companeroEnPartida = resultado == Resultado.VICTORIA_LOCAL;
        textoCentrado(companeroEnPartida ? "¡Victoria!" : "¡Victoria cooperativa!", centro, altura + 64f,
            Color.GREEN);
        if (companeroEnPartida) {
            textoCentrado("Tu compañero sigue en partida", centro, altura - 24f, Color.WHITE);
        }
        textoCentrado("Enemigos eliminados: " + enemigosEliminados, centro, altura + 20f, Color.WHITE);
        textoCentrado("Presiona ESC para volver al menú", centro, altura - (companeroEnPartida ? 68f : 24f), Color.WHITE);
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
