package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class SalaMultijugador extends ScreenAdapter {

    private final CartoonSurvivors game;
    private final ClienteUDP clienteUDP;

    private Stage stage;

    private Label estadoConexion;
    private Label numeroJugador;
    private Texture texturaRoja;
    private Texture texturaRojaPresionada;
    private Texture texturaVerde;
    private Texture texturaVerdePresionada;

    private TextButton.TextButtonStyle estiloNoListo;
    private TextButton.TextButtonStyle estiloListo;

    private TextButton botonListo;

    private boolean listo = false;
    private boolean cambiandoPantalla = false;

    public SalaMultijugador(CartoonSurvivors game) {
        this.game = game;
        this.clienteUDP = game.getClienteUDP();
    }

    @Override
    public void show() {

        stage = new Stage(new ExtendViewport(Constantes.Mundo.ANCHO_INTERFAZ, Constantes.Mundo.ALTO_INTERFAZ), game.getBatch());

        Gdx.input.setInputProcessor(stage);

        crearInterfaz();

        clienteUDP.conectar();
    }

    private void crearInterfaz() {

        Label.LabelStyle estiloTitulo = new Label.LabelStyle(game.getFont(), Color.YELLOW);

        Label.LabelStyle estiloTexto = new Label.LabelStyle(game.getFont(), Color.WHITE);

        TextButton.TextButtonStyle estiloBoton = new TextButton.TextButtonStyle();

        estiloBoton.font = game.getFont();
        estiloBoton.fontColor = Color.WHITE;
        estiloBoton.overFontColor = new Color(161f / 255f, 251f / 255f, 255f, 1.0f);
        estiloBoton.downFontColor = new Color(206f / 255f, 38f / 255f, 90f / 255f, 1.0f);


        Label titulo = new Label("SALA MULTIJUGADOR", estiloTitulo);

        estadoConexion = new Label("Buscando servidor...", estiloTexto);

        numeroJugador = new Label("Esperando conexion...", estiloTexto);



        texturaRoja = crearTexturaColor(
            new Color(0.80f, 0.12f, 0.16f, 1f)
        );

        texturaRojaPresionada = crearTexturaColor(
            new Color(0.55f, 0.08f, 0.12f, 1f)
        );

        texturaVerde = crearTexturaColor(
            new Color(0.12f, 0.65f, 0.25f, 1f)
        );

        texturaVerdePresionada = crearTexturaColor(
            new Color(0.08f, 0.45f, 0.18f, 1f)
        );

// ESTILO ROJO
        estiloNoListo = new TextButton.TextButtonStyle();

        estiloNoListo.font = game.getFont();
        estiloNoListo.fontColor = Color.WHITE;

        estiloNoListo.up = new TextureRegionDrawable(
            new TextureRegion(texturaRoja)
        );

        estiloNoListo.down = new TextureRegionDrawable(
            new TextureRegion(texturaRojaPresionada)
        );

        estiloNoListo.over = estiloNoListo.up;

// Deshabilitado, pero se sigue viendo rojo
        estiloNoListo.disabled = estiloNoListo.up;

// ESTILO VERDE
        estiloListo = new TextButton.TextButtonStyle();

        estiloListo.font = game.getFont();
        estiloListo.fontColor = Color.WHITE;

        estiloListo.up = new TextureRegionDrawable(
            new TextureRegion(texturaVerde)
        );

        estiloListo.down = new TextureRegionDrawable(
            new TextureRegion(texturaVerdePresionada)
        );

        estiloListo.over = estiloListo.up;

        estiloListo.disabled = estiloListo.up;

        botonListo = new TextButton(
            "NO LISTO",
            estiloNoListo
        );

        botonListo.setDisabled(true);

        botonListo.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (!clienteUDP.estaConectado()
                    || botonListo.isDisabled()
                    || cambiandoPantalla) {
                    return;
                }

                cambiarEstadoListo();
            }
        });

        TextButton botonVolver = new TextButton("VOLVER", estiloBoton);

        botonVolver.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (cambiandoPantalla) {
                    return;
                }

                clienteUDP.desconectar();

                game.setScreen(new MenuPantalla(game));
            }
        });

        Table tabla = new Table();
        tabla.setFillParent(true);
        stage.addActor(tabla);

        tabla.add(titulo).padBottom(40);
        tabla.row();

        tabla.add(estadoConexion).padBottom(20);
        tabla.row();

        tabla.add(numeroJugador).padBottom(30);
        tabla.row();


        tabla.add(botonListo).width(300).height(65).padBottom(25);

        tabla.row();

        tabla.add(botonVolver).width(300).height(65);
    }

    private Texture crearTexturaColor(Color color) {

        Pixmap pixmap = new Pixmap(
            1, 1,
            Pixmap.Format.RGBA8888
        );

        pixmap.setColor(color);
        pixmap.fill();

        Texture textura = new Texture(pixmap);

        pixmap.dispose();

        return textura;
    }

    private void cambiarEstadoListo() {

        listo = !listo;

        if (listo) {

            botonListo.setText("LISTO");
            botonListo.setStyle(estiloListo);

            clienteUDP.enviarMensaje("LISTO");

        } else {

            botonListo.setText("NO LISTO");
            botonListo.setStyle(estiloNoListo);

            clienteUDP.enviarMensaje("NO_LISTO");
        }
    }

    private void actualizarEstado() {

        if (!clienteUDP.estaConectado()) {

            estadoConexion.setText("Buscando servidor...");

            numeroJugador.setText("Esperando conexion...");

            botonListo.setDisabled(true);

            return;
        }

        botonListo.setDisabled(false);

        int id = clienteUDP.getIdJugador();

        numeroJugador.setText("jugador " + id);

        estadoConexion.setText("Conectado al servidor");
    }

    private void comprobarInicioPartida() {

        if (cambiandoPantalla) {
            return;
        }

        if (!clienteUDP.estaPartidaIniciada()) {
            return;
        }

        cambiandoPantalla = true;

        Gdx.app.log("SalaMultijugador", "EMPIEZA recibido. Entrando al juego...");

        game.setScreen(new JuegoPantalla(game));
    }

    @Override
    public void render(float delta) {

        // Primero comprobamos si el servidor
        // autorizó el inicio de la partida.
        comprobarInicioPartida();

        // Si cambiamos de pantalla, no seguimos
        // dibujando la sala anterior.
        if (cambiandoPantalla) {
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.16f, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        actualizarEstado();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {

        if (stage != null) {
            stage.dispose();
        }

        if (texturaRoja != null) {
            texturaRoja.dispose();
        }

        if (texturaVerde != null) {
            texturaVerde.dispose();
        }

    }
}
