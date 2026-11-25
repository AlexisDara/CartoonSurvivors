package com.cartoonsurvivors.game.pantallas;

import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.RondaMejora;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.armas.Espada;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.EstadisticasPartida;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.OpcionMejora;

import static com.cartoonsurvivors.game.pantallas.HUDJuego.TEXTO_HUD;

final class MenuMejoras implements Disposable {
    private static final int ANCHO_FONDO_MEJORAS = 860;
    private static final int ALTO_FONDO_MEJORAS = 416;
    private static final int ALTO_MENU = 370;
    private static final int TAMANIO_OPCION = 220;
    private static final int PASO_OPCION = 230;
    private static final Color CAPA_PAUSA = new Color(0x080d19bd);

    private final HUDJuego hud;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ExtendViewport hudViewport;
    private final Vector2 punteroHud = new Vector2();
    private final Texture espadaMejora = HUDJuego.cargarTextura("armas/espada.png");
    private final Texture pistolaMejora = HUDJuego.cargarTextura("armas/pistola.png");

    MenuMejoras(HUDJuego hud, SpriteBatch batch, BitmapFont font, ExtendViewport hudViewport) {
        this.hud = hud;
        this.batch = batch;
        this.font = font;
        this.hudViewport = hudViewport;
    }

    void dibujar(Jugador jugador, Jugador jugadorRemoto, EstadisticasPartida estadisticas,
                              int seleccion, boolean confirmando) {
        hud.prepararHUD();
        batch.begin();

        float centro = hudViewport.getWorldWidth() / 2f;
        float arriba = arribaMenuMejoras();
        RondaMejora ronda = estadisticas.getRondaMejora();
        int segundos = (ronda.getMilisegundos() + 999) / 1000;

        hud.rectangulo(CAPA_PAUSA, 0, 0, hudViewport.getWorldWidth(), hudViewport.getWorldHeight());
        float centroY = hudViewport.getWorldHeight() / 2f;
        hud.dibujarFondo(centro - ANCHO_FONDO_MEJORAS / 2f, centroY - ALTO_FONDO_MEJORAS / 2f,
            ANCHO_FONDO_MEJORAS, ALTO_FONDO_MEJORAS);
        float escalaX = font.getScaleX();
        float escalaY = font.getScaleY();
        font.getData().setScale(escalaX * 0.72f, escalaY * 0.72f);
        hud.textoCentrado("Nivel " + estadisticas.getNivel(), centro, arriba - 38, TEXTO_HUD);
        font.getData().setScale(escalaX * 0.39f, escalaY * 0.39f);
        hud.textoCentrado("Tiempo restante " + segundos + " s", centro, arriba - 70, TEXTO_HUD);
        int cantidad = ronda.getOpciones().size();
        float y = baseOpciones();
        for (int i = 0; i < cantidad; i++) {
            float x = izquierdaOpcion(i, cantidad);
            OpcionMejora mejora = ronda.getOpciones().get(i);
            hud.dibujarMarcoOpcion(i == seleccion, x, y, TAMANIO_OPCION);
            float centroOpcion = x + TAMANIO_OPCION / 2f;
            hud.textoCentrado(Integer.toString(i + 1), centroOpcion, y + 181, TEXTO_HUD);
            Texture icono = iconoMejora(mejora.getTipo(), jugador);
            if (icono != null) {
                hud.dibujarIcono(icono, centroOpcion, y + 133, 64);
            }
            font.setColor(TEXTO_HUD);
            int alturaTexto;
            if (icono == null) {
                alturaTexto = 133;
            } else {
                alturaTexto = 97;
            }
            font.draw(batch, etiquetaMejora(mejora, jugador),
                centroOpcion - 76, y + alturaTexto, 152, Align.center, true);
        }

        String indicacion;
        if (seleccion < 0) {
            indicacion = "Elegí con clic o con 1–3";
        } else if (confirmando) {
            indicacion = "Confirmando tu elección…";
        } else {
            indicacion = "Esperando a tu compañero";
        }
        hud.textoCentrado(indicacion, centro, arriba - 307, TEXTO_HUD);
        font.getData().setScale(escalaX * 0.55f, escalaY * 0.55f);
        hud.dibujarReaparicion(jugador, jugadorRemoto, centro, hudViewport.getWorldHeight() - 20);
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
        switch (mejora) {
            case "ARMA":
                if (desbloqueaPistola(jugador)) {
                    return pistolaMejora;
                }
                return espadaMejora;
            case "ESPADA":
                return espadaMejora;
            case "PISTOLA":
                return pistolaMejora;
            default:
                return hud.iconoPasiva(mejora);
        }
    }

    private boolean desbloqueaPistola(Jugador jugador) {
        return jugador.getArmas().get(0) instanceof Espada;
    }

    private String etiquetaMejora(OpcionMejora mejora, Jugador jugador) {
        switch (mejora.getTipo()) {
            case "ESPADA":
                return etiquetaArma("Espada", mejora);
            case "PISTOLA":
                return etiquetaArma("Pistola", mejora);
            case "VELOCIDAD":
                return "Velocidad de movimiento\n+" + mejora.valor(0) + " %";
            case "ARMA":
                if (desbloqueaPistola(jugador)) {
                    return "Desbloquear pistola";
                }
                return "Desbloquear espada";
            case "VIDA_MAXIMA":
                return "+" + mejora.valor(0) + " de vida máxima";
            case "REGENERACION":
                return "Regeneración\n+" + mejora.valor(0) + " vida por segundo";
            case "ARMADURA":
                return "Armadura\n+" + mejora.valor(0) + " puntos de reducción de daño";
            case "CRITICO":
                return "Críticos\n+" + mejora.valor(0) + " puntos de probabilidad\nDaño doble";
            default:
                return mejora.getTipo();
        }
    }

    private String etiquetaArma(String nombre, OpcionMejora mejora) {
        StringBuilder texto = new StringBuilder(nombre);
        boolean conRebotes = mejora.getValores().size() == 5;
        int balaExtra = 0;
        if (conRebotes) {
            balaExtra = mejora.valor(4);
        } else if (mejora.getValores().size() == 4) {
            balaExtra = mejora.valor(3);
        }
        if (balaExtra > 0) {
            texto.append(" · +").append(balaExtra).append(" bala");
        }
        if (mejora.valor(0) > 0) {
            texto.append("\nDaño +").append(mejora.valor(0)).append(" %");
        }
        if (conRebotes && mejora.valor(1) > 0) {
            texto.append("\n+1 rebote");
            if (mejora.valor(3) > 0) {
                texto.append(" · -").append(mejora.valor(3)).append(" pp");
            }
        } else if (!conRebotes && mejora.valor(1) > 0) {
            texto.append("\nAlcance +").append(mejora.valor(1)).append(" %");
        }
        if (mejora.valor(2) > 0) {
            texto.append("\nIntervalo -").append(mejora.valor(2)).append(" %");
        }
        return texto.toString();
    }

    @Override
    public void dispose() {
        espadaMejora.dispose();
        pistolaMejora.dispose();
    }
}
