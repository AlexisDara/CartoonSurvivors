
package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;

import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_MUNDO;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_MUNDO;

public class HUDJuego {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ExtendViewport hudViewport;

    public HUDJuego(
        SpriteBatch batch,
        BitmapFont font,
        ExtendViewport hudViewport) {

        this.batch = batch;
        this.font = font;
        this.hudViewport = hudViewport;
    }

    // HUD utilizado durante la partida multijugador.
    public void dibujarMultijugador(
        Jugador jugadorLocal,
        Jugador jugadorRemoto,
        int idJugadorLocal,
        boolean estadoRecibido) {

        prepararHUD();

        batch.begin();

        font.setColor(Color.WHITE);

        font.draw(batch,
            "JUGADOR " + idJugadorLocal,
            20, ALTO_MUNDO - 20);

        font.draw(batch,
            "MI VIDA: " + (int) Math.ceil(jugadorLocal.getVida()),
            20, ALTO_MUNDO - 60);

        font.draw(batch,
            "VIDA COMPAÑERO: "
                + (int) Math.ceil(jugadorRemoto.getVida()),
            20, ALTO_MUNDO - 100);

        if (!estadoRecibido) {
            font.draw(batch,
                "Esperando posiciones del servidor...",
                20, ALTO_MUNDO - 150);
        }

        batch.end();
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
