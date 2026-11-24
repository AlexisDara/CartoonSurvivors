package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.entidades.jugadores.Jugador;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.red.ProtocoloRed;
import com.cartoonsurvivors.game.utilidades.Constantes;
import com.cartoonsurvivors.game.utilidades.TipoMapa;

public class SalaMultijugador extends ScreenAdapter {

    private final CartoonSurvivors game;
    private final ClienteUDP clienteUDP;

    private Stage stage;

    private Label estadoConexion;
    private Label numeroJugador;
    private Label nombrePersonaje;
    private Label nombreMapa;
    private TextButton botonMapaAnterior;
    private TextButton botonMapaSiguiente;
    private TipoMapa mapaSeleccionado = TipoMapa.MAPA1;

    private TextButton botonListo;
    private TextButton botonAnterior;
    private TextButton botonSiguiente;

    private Texture texturaRoja;
    private Texture texturaRojaPresionada;
    private Texture texturaVerde;
    private Texture texturaVerdePresionada;
    private Texture texturaPersonaje;
    private boolean seleccionEnviada = false;
    private Image imagenPersonaje;

    private TextButton.TextButtonStyle estiloNoListo;
    private TextButton.TextButtonStyle estiloListo;

    // Personajes disponibles
    private final TipoPersonaje[] personajes = TipoPersonaje.values();

    private int indicePersonaje = 0;

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
        estiloBoton.overFontColor = Color.CYAN;
        estiloBoton.downFontColor = Color.PINK;

        Label titulo = new Label("SALA MULTIJUGADOR", estiloTitulo);

        estadoConexion = new Label("Buscando servidor...", estiloTexto);

        numeroJugador = new Label("Esperando conexion...", estiloTexto);

        // SELECCION DE PERSONAJE

        Label tituloSeleccion = new Label("Seleccionar personaje", estiloTexto);

        nombrePersonaje = new Label(getPersonajeSeleccionado().getNombre(), estiloTexto);
        nombrePersonaje.setAlignment(Align.center);

        // Imagen del personaje disponible
        texturaPersonaje = new Texture(getPersonajeSeleccionado().getRutaIdle());

        imagenPersonaje = new Image(texturaPersonaje);

        botonAnterior = new TextButton("<", estiloBoton);

        botonSiguiente = new TextButton(">", estiloBoton);

        botonAnterior.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                cambiarPersonaje(-1);
            }
        });

        botonSiguiente.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                cambiarPersonaje(1);
            }
        });

        // TEXTURAS DEL BOTON LISTO

        texturaRoja = crearTexturaColor(new Color(0.80f, 0.12f, 0.16f, 1f));

        texturaRojaPresionada = crearTexturaColor(new Color(0.55f, 0.08f, 0.12f, 1f));

        texturaVerde = crearTexturaColor(new Color(0.12f, 0.65f, 0.25f, 1f));

        texturaVerdePresionada = crearTexturaColor(new Color(0.08f, 0.45f, 0.18f, 1f));

        // ESTILO NO LISTO

        estiloNoListo = new TextButton.TextButtonStyle();

        estiloNoListo.font = game.getFont();
        estiloNoListo.fontColor = Color.WHITE;

        estiloNoListo.up = new TextureRegionDrawable(new TextureRegion(texturaRoja));

        estiloNoListo.down = new TextureRegionDrawable(new TextureRegion(texturaRojaPresionada));

        estiloNoListo.over = estiloNoListo.up;
        estiloNoListo.disabled = estiloNoListo.up;

        // ESTILO LISTO

        estiloListo = new TextButton.TextButtonStyle();

        estiloListo.font = game.getFont();
        estiloListo.fontColor = Color.WHITE;

        estiloListo.up = new TextureRegionDrawable(new TextureRegion(texturaVerde));

        estiloListo.down = new TextureRegionDrawable(new TextureRegion(texturaVerdePresionada));

        estiloListo.over = estiloListo.up;
        estiloListo.disabled = estiloListo.up;

        // BOTON LISTO

        botonListo = new TextButton("NO LISTO", estiloNoListo);

        botonListo.setDisabled(true);

        botonListo.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (!clienteUDP.estaConectado() || botonListo.isDisabled() || cambiandoPantalla) {
                    return;
                }

                cambiarEstadoListo();
            }
        });

        // BOTON VOLVER

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

        // DISTRIBUCION DE LA SALA

        Table tabla = new Table();
        tabla.setFillParent(true);

        stage.addActor(tabla);

        tabla.add(titulo).padBottom(12);
        tabla.row();

        tabla.add(estadoConexion).padBottom(10);
        tabla.row();

        tabla.add(numeroJugador).padBottom(12);
        tabla.row();

        tabla.add(tituloSeleccion).padBottom(10);
        tabla.row();

        tabla.add(imagenPersonaje).width(95).height(110).padBottom(6);

        tabla.row();

        Table selector = new Table();

        selector.add(botonAnterior).width(50).height(40).padRight(14);

        selector.add(nombrePersonaje).width(260);

        selector.add(botonSiguiente).width(50).height(40).padLeft(14);

        tabla.add(selector).padBottom(12);
        tabla.row();

        Label tituloMapa = new Label("Mapa (elige el jugador 1)", estiloTexto);
        nombreMapa = new Label(mapaSeleccionado.getNombre(), estiloTexto);
        nombreMapa.setAlignment(Align.center);
        botonMapaAnterior = crearBotonMapa("<", estiloBoton, -1);
        botonMapaSiguiente = crearBotonMapa(">", estiloBoton, 1);

        Table selectorMapa = new Table();
        selectorMapa.add(botonMapaAnterior).width(50).height(40).padRight(14);
        selectorMapa.add(nombreMapa).width(260);
        selectorMapa.add(botonMapaSiguiente).width(50).height(40).padLeft(14);

        tabla.add(tituloMapa).padBottom(5);
        tabla.row();
        tabla.add(selectorMapa).padBottom(10);
        tabla.row();

        botonListo.getLabel().setFontScale(0.8f);
        tabla.add(botonListo).width(230).height(48).padBottom(8);

        tabla.row();

        botonVolver.getLabel().setFontScale(0.8f);
        tabla.add(botonVolver).width(230).height(48);

        actualizarBotonesSeleccion();
    }

    // DEVUELVE EL TIPO SELECCIONADO

    private TipoPersonaje getPersonajeSeleccionado() {
        return personajes[indicePersonaje];
    }

    // CAMBIA ENTRE PERSONAJES

    private void cambiarPersonaje(int direccion) {

        if (listo || cambiandoPantalla || personajes.length <= 1) {
            return;
        }

        indicePersonaje += direccion;

        if (indicePersonaje < 0) {
            indicePersonaje = personajes.length - 1;
        }

        if (indicePersonaje >= personajes.length) {
            indicePersonaje = 0;
        }

        nombrePersonaje.setText(getPersonajeSeleccionado().getNombre());

        Texture anterior = texturaPersonaje;
        texturaPersonaje = new Texture(getPersonajeSeleccionado().getRutaIdle());
        imagenPersonaje.setDrawable(new TextureRegionDrawable(new TextureRegion(texturaPersonaje)));
        anterior.dispose();

        Gdx.app.log("SalaMultijugador", "Personaje seleccionado: " + getPersonajeSeleccionado().name());
    }

    private TextButton crearBotonMapa(String texto, TextButton.TextButtonStyle estilo, int direccion) {
        TextButton boton = new TextButton(texto, estilo);
        boton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!puedeElegirMapa()) {
                    return;
                }
                TipoMapa[] mapas = TipoMapa.values();
                mapaSeleccionado = mapas[Math.floorMod(mapaSeleccionado.ordinal() + direccion, mapas.length)];
                nombreMapa.setText(mapaSeleccionado.getNombre());
                clienteUDP.enviarMensaje(ProtocoloRed.MAPA + "|" + mapaSeleccionado.name());
            }
        });
        return boton;
    }

    private boolean puedeElegirMapa() {
        return clienteUDP.estaConectado() && !clienteUDP.estaServidorDesconectado()
            && clienteUDP.getIdJugador() == 1 && !listo && !cambiandoPantalla;
    }

    private void actualizarBotonesSeleccion() {

        boolean deshabilitar = listo || cambiandoPantalla || personajes.length <= 1;

        botonAnterior.setDisabled(deshabilitar);
        botonSiguiente.setDisabled(deshabilitar);
        boolean deshabilitarMapa = !puedeElegirMapa();
        botonMapaAnterior.setDisabled(deshabilitarMapa);
        botonMapaSiguiente.setDisabled(deshabilitarMapa);
    }

    // CREAR TEXTURA DE COLOR

    private Texture crearTexturaColor(Color color) {

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(color);
        pixmap.fill();

        Texture textura = new Texture(pixmap);

        pixmap.dispose();

        return textura;
    }

    // CAMBIAR ESTADO LISTO

    private void cambiarEstadoListo() {

        listo = !listo;

        if (listo) {

            clienteUDP.enviarSeleccionPersonaje(
                getPersonajeSeleccionado()
            );

            botonListo.setText("LISTO");
            botonListo.setStyle(estiloListo);

            String mensaje = ProtocoloRed.LISTO;
            if (clienteUDP.getIdJugador() == 1) {
                mensaje += "|" + mapaSeleccionado.name();
            }
            clienteUDP.enviarMensaje(mensaje);

        } else {

            botonListo.setText("NO LISTO");
            botonListo.setStyle(estiloNoListo);

            clienteUDP.enviarMensaje("NO_LISTO");
        }

        actualizarBotonesSeleccion();
    }

    // ACTUALIZAR ESTADO DE CONEXION


    private void actualizarEstado() {
        actualizarBotonesSeleccion();
        if (clienteUDP.getIdJugador() != 1) {
            nombreMapa.setText(clienteUDP.getMapaLobby().getNombre());
        }


        if (clienteUDP.estaServidorDesconectado()) {

            estadoConexion.setText(
                "SE PERDIO LA CONEXION CON EL SERVIDOR"
            );

            estadoConexion.setColor(Color.RED);

            numeroJugador.setText(
                "Servidor desconectado"
            );

            botonListo.setDisabled(true);

            return;
        }

        if (!clienteUDP.estaConectado()) {

            estadoConexion.setText("Buscando servidor...");
            numeroJugador.setText("Esperando conexion...");

            botonListo.setDisabled(true);
            seleccionEnviada = false;

            return;
        }

        botonListo.setDisabled(false);

        int id = clienteUDP.getIdJugador();

        numeroJugador.setText("Jugador " + id);
        estadoConexion.setText("Conectado al servidor");

        if (!seleccionEnviada) {

            clienteUDP.enviarSeleccionPersonaje(
                getPersonajeSeleccionado()
            );

            seleccionEnviada = true;
        }
    }


    // INICIAR PARTIDA

    private void comprobarInicioPartida() {

        if (cambiandoPantalla) {
            return;
        }

        if (!clienteUDP.estaPartidaIniciada()) {
            return;
        }

        cambiandoPantalla = true;

        TipoPersonaje tipo = getPersonajeSeleccionado();

        Jugador jugador = tipo.crearPersonaje();

        Gdx.app.log("SalaMultijugador", "Iniciando partida con " + tipo.getNombre());

        game.setScreen(new JuegoPantalla(game, jugador));
    }

    @Override
    public void render(float delta) {

        comprobarInicioPartida();

        if (cambiandoPantalla) {
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.16f, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        actualizarEstado();

        stage.act(delta);

        game.getBatch().setColor(Color.WHITE);
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

        if (texturaRojaPresionada != null) {
            texturaRojaPresionada.dispose();
        }

        if (texturaVerde != null) {
            texturaVerde.dispose();
        }

        if (texturaVerdePresionada != null) {
            texturaVerdePresionada.dispose();
        }

        if (texturaPersonaje != null) {
            texturaPersonaje.dispose();
        }
    }
}
