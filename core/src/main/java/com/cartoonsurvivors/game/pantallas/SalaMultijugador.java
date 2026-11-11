
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

import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class SalaMultijugador extends ScreenAdapter {

    private final CartoonSurvivors game;
    private final ClienteUDP clienteUDP;

    private Stage stage;

    private Label titulo;
    private Label estadoConexion;
    private Label numeroJugador;
    private Label estadoListo;

    private TextButton botonListo;

    private boolean listo = false;

    public SalaMultijugador(CartoonSurvivors game) {
        this.game = game;
        this.clienteUDP = game.getClienteUDP();
    }

    @Override
    public void show() {

        stage = new Stage(
            new ExtendViewport(
                Constantes.Mundo.ANCHO_INTERFAZ,
                Constantes.Mundo.ALTO_INTERFAZ
            ),
            game.getBatch()
        );

        Gdx.input.setInputProcessor(stage);

        crearInterfaz();

        clienteUDP.conectar();
    }

    private void crearInterfaz() {

        Label.LabelStyle estiloTitulo =
            new Label.LabelStyle(
                game.getFont(),
                Color.YELLOW
            );

        Label.LabelStyle estiloTexto =
            new Label.LabelStyle(
                game.getFont(),
                Color.WHITE
            );

        titulo = new Label(
            "SALA MULTIJUGADOR",
            estiloTitulo
        );

        estadoConexion = new Label(
            "Buscando servidor...",
            estiloTexto
        );

        numeroJugador = new Label(
            "Esperando conexion...",
            estiloTexto
        );

        estadoListo = new Label(
            "NO LISTO",
            estiloTexto
        );

        // ESTILO DE BOTONES

        TextButton.TextButtonStyle estiloBoton =
            new TextButton.TextButtonStyle();

        estiloBoton.font = game.getFont();
        estiloBoton.fontColor = Color.WHITE;
        estiloBoton.overFontColor = Color.YELLOW;
        estiloBoton.downFontColor = Color.GREEN;

        // BOTON LISTO

        botonListo = new TextButton(
            "LISTO",
            estiloBoton
        );

        botonListo.setDisabled(true);

        botonListo.addListener(new ClickListener() {

            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y
            ) {

                if (!clienteUDP.estaConectado()) {
                    return;
                }

                cambiarEstadoListo();
            }
        });

        // BOTON VOLVER

        TextButton botonVolver =
            new TextButton("VOLVER", estiloBoton);

        botonVolver.addListener(new ClickListener() {

            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y
            ) {

                clienteUDP.desconectar();

                game.setScreen(
                    new MenuPantalla(game)
                );
            }
        });

        // TABLA

        Table tabla = new Table();
        tabla.setFillParent(true);

        stage.addActor(tabla);

        tabla.add(titulo).padBottom(50);
        tabla.row();

        tabla.add(estadoConexion).padBottom(30);
        tabla.row();

        tabla.add(numeroJugador).padBottom(30);
        tabla.row();

        tabla.add(estadoListo).padBottom(40);
        tabla.row();

        tabla.add(botonListo)
            .width(300)
            .height(65)
            .padBottom(25);

        tabla.row();

        tabla.add(botonVolver)
            .width(300)
            .height(65);
    }

    // CAMBIAR ESTADO LISTO

    private void cambiarEstadoListo() {

        listo = !listo;

        if (listo) {

            botonListo.setText("CANCELAR LISTO");

            estadoListo.setText("LISTO");
            estadoListo.setColor(Color.GREEN);

            clienteUDP.enviarMensaje("LISTO");

        } else {

            botonListo.setText("LISTO");

            estadoListo.setText("NO LISTO");
            estadoListo.setColor(Color.WHITE);

            clienteUDP.enviarMensaje("NO_LISTO");
        }
    }

    // ACTUALIZAR INFORMACION DE LA SALA

    private void actualizarEstado() {

        if (!clienteUDP.estaConectado()) {

            estadoConexion.setText(
                "Buscando servidor..."
            );

            numeroJugador.setText(
                "Esperando conexion..."
            );

            botonListo.setDisabled(true);

            return;
        }

        botonListo.setDisabled(false);

        int id = clienteUDP.getIdJugador();

        numeroJugador.setText(
            "Jugador " + id
        );

        if (clienteUDP.estaPartidaIniciada()) {

            estadoConexion.setText(
                "La partida ha comenzado!"
            );

        } else {

            estadoConexion.setText(
                "Esperando jugadores..."
            );
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(
            0.08f,
            0.10f,
            0.16f,
            1
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        clienteUDP.actualizar();

        actualizarEstado();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        stage.getViewport().update(
            width,
            height,
            true
        );
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
    }
}
