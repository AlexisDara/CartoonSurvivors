package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.utilidades.TipoMapa;


import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ALTO_INTERFAZ;
import static com.cartoonsurvivors.game.utilidades.Constantes.Mundo.ANCHO_INTERFAZ;

final class InterfazSala implements Disposable {
    private final Stage stage;
    private final Texture pixel;
    private Texture texturaPersonaje;
    private final Image imagenPersonaje;
    private final Label estadoConexion;
    private final Label numeroJugador;
    private final Label nombrePersonaje;
    private final Label nombreMapa;
    private final TextButton personajeAnterior;
    private final TextButton personajeSiguiente;
    private final TextButton mapaAnterior;
    private final TextButton mapaSiguiente;
    private final TextButton botonListo;
    private final TextButton botonVolver;
    private final TextButton.TextButtonStyle estiloNoListo;
    private final TextButton.TextButtonStyle estiloListo;

    InterfazSala(CartoonSurvivors game, TipoPersonaje personaje, TipoMapa mapa) {
        stage = new Stage(new ExtendViewport(ANCHO_INTERFAZ, ALTO_INTERFAZ), game.getBatch());
        Pixmap imagen = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        imagen.setColor(Color.WHITE);
        imagen.fill();
        pixel = new Texture(imagen);
        imagen.dispose();
        TextureRegionDrawable base = new TextureRegionDrawable(new TextureRegion(pixel));

        Label.LabelStyle texto = new Label.LabelStyle(game.getFont(), Color.WHITE);
        Label.LabelStyle titulo = new Label.LabelStyle(game.getFont(), Color.YELLOW);
        TextButton.TextButtonStyle boton = new TextButton.TextButtonStyle();
        boton.font = game.getFont();
        boton.fontColor = Color.WHITE;
        boton.overFontColor = Color.CYAN;
        boton.downFontColor = Color.PINK;
        estiloNoListo = estiloListo(boton, base, new Color(0.80f, 0.12f, 0.16f, 1f), new Color(0.55f, 0.08f, 0.12f, 1f));
        estiloListo = estiloListo(boton, base, new Color(0.12f, 0.65f, 0.25f, 1f), new Color(0.08f, 0.45f, 0.18f, 1f));

        estadoConexion = new Label("Buscando servidor...", texto);
        numeroJugador = new Label("Esperando conexión...", texto);
        nombrePersonaje = new Label(personaje.getNombre(), texto);
        nombreMapa = new Label(mapa.getNombre(), texto);
        texturaPersonaje = new Texture(personaje.getRutaIdle());
        imagenPersonaje = new Image(texturaPersonaje);
        personajeAnterior = new TextButton("<", boton);
        personajeSiguiente = new TextButton(">", boton);
        mapaAnterior = new TextButton("<", boton);
        mapaSiguiente = new TextButton(">", boton);
        botonListo = new TextButton("NO LISTO", estiloNoListo);
        botonListo.setDisabled(true);
        botonListo.getLabel().setFontScale(0.8f);
        botonVolver = new TextButton("VOLVER", boton);
        botonVolver.getLabel().setFontScale(0.8f);

        Table tabla = new Table();
        tabla.setFillParent(true);
        tabla.add(new Label("SALA MULTIJUGADOR", titulo)).padBottom(12).row();
        tabla.add(estadoConexion).padBottom(10).row();
        tabla.add(numeroJugador).padBottom(12).row();
        tabla.add(new Label("Seleccionar personaje", texto)).padBottom(10).row();
        tabla.add(imagenPersonaje).width(95).height(110).padBottom(6).row();
        tabla.add(selector(personajeAnterior, nombrePersonaje, personajeSiguiente)).padBottom(12).row();
        tabla.add(new Label("Mapa (elige el jugador 1)", texto)).padBottom(5).row();
        tabla.add(selector(mapaAnterior, nombreMapa, mapaSiguiente)).padBottom(10).row();
        tabla.add(botonListo).width(230).height(48).padBottom(8).row();
        tabla.add(botonVolver).width(230).height(48);
        stage.addActor(tabla);
    }

    private static Table selector(TextButton anterior, Label nombre, TextButton siguiente) {
        Table tabla = new Table();
        nombre.setAlignment(Align.center);
        tabla.add(anterior).width(50).height(40).padRight(14);
        tabla.add(nombre).width(260);
        tabla.add(siguiente).width(50).height(40).padLeft(14);
        return tabla;
    }

    private static TextButton.TextButtonStyle estiloListo(TextButton.TextButtonStyle boton,
            TextureRegionDrawable base, Color color, Color presionado) {
        TextButton.TextButtonStyle estilo = new TextButton.TextButtonStyle();
        estilo.font = boton.font;
        estilo.fontColor = Color.WHITE;
        estilo.up = base.tint(color);
        estilo.down = base.tint(presionado);
        estilo.over = estilo.up;
        estilo.disabled = estilo.up;
        return estilo;
    }

    void conectarAcciones(SalaMultijugador sala) {
        personajeAnterior.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!personajeAnterior.isDisabled()) {
                    sala.cambiarPersonaje(-1);
                }
            }
        });

        personajeSiguiente.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!personajeSiguiente.isDisabled()) {
                    sala.cambiarPersonaje(1);
                }
            }
        });

        mapaAnterior.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!mapaAnterior.isDisabled()) {
                    sala.cambiarMapa(-1);
                }
            }
        });

        mapaSiguiente.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!mapaSiguiente.isDisabled()) {
                    sala.cambiarMapa(1);
                }
            }
        });

        botonListo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!botonListo.isDisabled()) {
                    sala.cambiarEstadoListo();
                }
            }
        });

        botonVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!botonVolver.isDisabled()) {
                    sala.volver();
                }
            }
        });
    }

    Stage stage() {
        return stage;
    }

    void mostrarPersonaje(TipoPersonaje personaje) {
        nombrePersonaje.setText(personaje.getNombre());
        Texture anterior = texturaPersonaje;
        texturaPersonaje = new Texture(personaje.getRutaIdle());
        imagenPersonaje.setDrawable(new TextureRegionDrawable(new TextureRegion(texturaPersonaje)));
        anterior.dispose();
    }

    void mostrarMapa(TipoMapa mapa) {
        nombreMapa.setText(mapa.getNombre());
    }

    void mostrarConexion(String estado, String jugador, Color color, boolean conectado) {
        estadoConexion.setText(estado);
        estadoConexion.setColor(color);
        numeroJugador.setText(jugador);
        botonListo.setDisabled(!conectado);
    }

    void habilitarSelectores(boolean personaje, boolean mapa) {
        personajeAnterior.setDisabled(!personaje);
        personajeSiguiente.setDisabled(!personaje);
        mapaAnterior.setDisabled(!mapa);
        mapaSiguiente.setDisabled(!mapa);
    }

    boolean listoDeshabilitado() {
        return botonListo.isDisabled();
    }

    void mostrarListo(boolean listo) {
        botonListo.setText(listo ? "LISTO" : "NO LISTO");
        botonListo.setStyle(listo ? estiloListo : estiloNoListo);
    }

    @Override
    public void dispose() {
        stage.dispose();
        pixel.dispose();
        texturaPersonaje.dispose();
    }
}
