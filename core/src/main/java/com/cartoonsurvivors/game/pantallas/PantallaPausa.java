package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.sistemas.SincronizadorJugadores.Pausa;

public class PantallaPausa {
    private final Stage stage;
    private final AudioManager audio;
    private final Texture fondo = new Texture("Hud/fondo-mejora.png");
    private final Texture pixel;
    private final Label aviso;
    private final Label tiempo;
    private final Label estado;
    private final Label volumen;
    private final TextButton continuar;

    PantallaPausa(CartoonSurvivors game, Viewport viewport, JuegoPantalla juego) {
        stage = new Stage(viewport, game.getBatch());
        audio = game.getAudioManager();

        Pixmap imagen = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        imagen.setColor(Color.WHITE);
        imagen.fill();
        pixel = new Texture(imagen);
        imagen.dispose();
        TextureRegionDrawable base = new TextureRegionDrawable(new TextureRegion(pixel));

        Label.LabelStyle etiquetas = new Label.LabelStyle(game.getFont(), Color.WHITE);
        TextButton.TextButtonStyle botones = new TextButton.TextButtonStyle();
        botones.font = game.getFont();
        botones.fontColor = Color.WHITE;
        botones.downFontColor = new Color(0xffbd62ff);
        botones.disabledFontColor = Color.LIGHT_GRAY;

        Table oscurecer = new Table();
        oscurecer.setFillParent(true);
        oscurecer.setBackground(base.tint(new Color(0x080d19bb)));
        stage.addActor(oscurecer);

        Table menu = new Table();
        TextureRegionDrawable panel = new TextureRegionDrawable(new TextureRegion(fondo));
        panel.setMinWidth(0f);
        panel.setMinHeight(0f);
        menu.setBackground(panel);
        menu.pad(32f);
        oscurecer.add(menu).width(608f);

        aviso = etiqueta(etiquetas, 0.55f);
        tiempo = etiqueta(etiquetas, 0.55f);
        estado = etiqueta(etiquetas, 0.5f);
        volumen = etiqueta(etiquetas, 0.55f);
        continuar = boton("Reanudar", botones);
        continuar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!continuar.isDisabled()) {
                    juego.solicitarReanudacion();
                }
            }
        });

        Label titulo = etiqueta(etiquetas, 0.85f);
        titulo.setText("Partida en pausa");
        menu.add(titulo).padBottom(10f).row();
        menu.add(aviso).height(28f).row();
        menu.add(tiempo).height(28f).row();
        menu.add(estado).height(28f).padBottom(12f).row();
        menu.add(continuar).width(240f).height(45f).padBottom(12f).row();

        Table ajuste = new Table();
        ajuste.add(botonVolumen("-", botones, -10)).size(42f);
        ajuste.add(volumen).width(200f);
        ajuste.add(botonVolumen("+", botones, 10)).size(42f);
        menu.add(ajuste).padBottom(12f).row();
        TextButton salir = boton("Salir", botones);
        salir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!salir.isDisabled()) {
                    juego.solicitarSalida();
                }
            }
        });
        menu.add(salir).width(240f).height(45f);
        actualizarVolumen();
    }

    private static Label etiqueta(Label.LabelStyle estilo, float escala) {
        Label etiqueta = new Label("", estilo);
        etiqueta.setAlignment(Align.center);
        etiqueta.setFontScale(escala);
        return etiqueta;
    }

    private static TextButton boton(String texto, TextButton.TextButtonStyle estilo) {
        TextButton boton = new TextButton(texto, estilo);
        boton.getLabel().setFontScale(0.65f);
        return boton;
    }

    private TextButton botonVolumen(String texto, TextButton.TextButtonStyle estilo, int cambio) {
        TextButton boton = boton(texto, estilo);
        boton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!boton.isDisabled()) {
                    ajustarVolumen(cambio);
                }
            }
        });
        return boton;
    }

    private void ajustarVolumen(int cambio) {
        int porcentaje = Math.round(audio.getVolumenMusica() * 100f);
        audio.setVolumenMusica((porcentaje + cambio) / 100f);
        actualizarVolumen();
    }

    private void actualizarVolumen() {
        int porcentaje = Math.round(audio.getVolumenMusica() * 100f);
        volumen.setText("Volumen " + porcentaje + " %");
    }

    void render(float delta, Pausa pausa, int idLocal, boolean enviandoConfirmacion) {
        aviso.setText(pausa.solicitante() == idLocal ? "Pausaste la partida" : "Tu compañero pausó la partida");
        int segundos = (pausa.milisegundos() + 999) / 1000;
        tiempo.setText("Reanudación automática en " + segundos + " s");
        boolean confirmado = pausa.confirmado(idLocal);
        continuar.setDisabled(confirmado || enviandoConfirmacion);
        if (confirmado) {
            estado.setText("Esperando a tu compañero");
        } else if (enviandoConfirmacion) {
            estado.setText("Confirmando…");
        } else if (pausa.confirmado(3 - idLocal)) {
            estado.setText("Tu compañero ya confirmó");
        } else {
            estado.setText("Ambos deben confirmar para volver antes");
        }
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
        pixel.dispose();
        fondo.dispose();
    }
}
