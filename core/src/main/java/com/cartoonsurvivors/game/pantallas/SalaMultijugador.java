package com.cartoonsurvivors.game.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.cartoonsurvivors.game.CartoonSurvivors;
import com.cartoonsurvivors.game.entidades.jugadores.TipoPersonaje;
import com.cartoonsurvivors.game.red.ClienteUDP;
import com.cartoonsurvivors.game.red.ProtocoloRed;
import com.cartoonsurvivors.game.utilidades.TipoMapa;

public class SalaMultijugador extends ScreenAdapter {
    private final CartoonSurvivors game;
    private final ClienteUDP clienteUDP;
    private final TipoPersonaje[] personajes = TipoPersonaje.values();
    private final TipoMapa[] mapas = TipoMapa.values();
    private InterfazSala interfaz;
    private TipoMapa mapaSeleccionado = TipoMapa.MAPA1;
    private int indicePersonaje;
    private boolean seleccionEnviada;
    private boolean listo;
    private boolean cambiandoPantalla;

    public SalaMultijugador(CartoonSurvivors game) {
        this.game = game;
        clienteUDP = game.getClienteUDP();
    }

    @Override
    public void show() {
        interfaz = new InterfazSala(game, personajes[indicePersonaje], mapaSeleccionado);
        interfaz.conectarAcciones(this::cambiarPersonaje, this::cambiarMapa, this::cambiarEstadoListo, this::volver);
        Gdx.input.setInputProcessor(interfaz.stage());
        actualizarBotonesSeleccion();
        clienteUDP.conectar();
    }

    private void cambiarPersonaje(int direccion) {
        if (listo || cambiandoPantalla || personajes.length <= 1) {
            return;
        }
        indicePersonaje = Math.floorMod(indicePersonaje + direccion, personajes.length);
        interfaz.mostrarPersonaje(personajes[indicePersonaje]);
        Gdx.app.log("SalaMultijugador", "Personaje seleccionado: " + personajes[indicePersonaje].name());
    }

    private void cambiarMapa(int direccion) {
        if (!puedeElegirMapa()) {
            return;
        }
        mapaSeleccionado = mapas[Math.floorMod(mapaSeleccionado.ordinal() + direccion, mapas.length)];
        interfaz.mostrarMapa(mapaSeleccionado);
        clienteUDP.enviarMensaje(ProtocoloRed.MAPA + "|" + mapaSeleccionado.name());
    }

    private boolean puedeElegirMapa() {
        return clienteUDP.estaConectado() && !clienteUDP.estaServidorDesconectado()
            && clienteUDP.getIdJugador() == 1 && !listo && !cambiandoPantalla;
    }

    private void actualizarBotonesSeleccion() {
        interfaz.habilitarSelectores(!listo && !cambiandoPantalla && personajes.length > 1, puedeElegirMapa());
    }

    private void cambiarEstadoListo() {
        if (!clienteUDP.estaConectado() || interfaz.listoDeshabilitado() || cambiandoPantalla) {
            return;
        }
        listo = !listo;
        interfaz.mostrarListo(listo);
        if (listo) {
            clienteUDP.enviarSeleccionPersonaje(personajes[indicePersonaje]);
            String mensaje = ProtocoloRed.LISTO;
            if (clienteUDP.getIdJugador() == 1) {
                mensaje += "|" + mapaSeleccionado.name();
            }
            clienteUDP.enviarMensaje(mensaje);
        } else {
            clienteUDP.enviarMensaje("NO_LISTO");
        }
        actualizarBotonesSeleccion();
    }

    private void actualizarEstado() {
        actualizarBotonesSeleccion();
        int id = clienteUDP.getIdJugador();
        if (id != 1) {
            interfaz.mostrarMapa(clienteUDP.getMapaLobby());
        }
        if (clienteUDP.estaServidorDesconectado()) {
            interfaz.mostrarConexion("SE PERDIÓ LA CONEXIÓN CON EL SERVIDOR", "Servidor desconectado", Color.RED, false);
            return;
        }
        if (!clienteUDP.estaConectado()) {
            interfaz.mostrarConexion("Buscando servidor...", "Esperando conexión...", Color.WHITE, false);
            seleccionEnviada = false;
            return;
        }
        interfaz.mostrarConexion("Conectado al servidor", "Jugador " + id, Color.WHITE, true);
        if (!seleccionEnviada) {
            clienteUDP.enviarSeleccionPersonaje(personajes[indicePersonaje]);
            seleccionEnviada = true;
        }
    }

    private void volver() {
        if (!cambiandoPantalla) {
            clienteUDP.desconectar();
            game.setScreen(new MenuPantalla(game));
        }
    }

    @Override
    public void render(float delta) {
        if (clienteUDP.estaPartidaIniciada() && !cambiandoPantalla) {
            cambiandoPantalla = true;
            TipoPersonaje tipo = personajes[indicePersonaje];
            Gdx.app.log("SalaMultijugador", "Iniciando partida con " + tipo.getNombre());
            game.setScreen(new JuegoPantalla(game, tipo.crearPersonaje()));
        }
        if (cambiandoPantalla) {
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        actualizarEstado();
        interfaz.stage().act(delta);
        game.getBatch().setColor(Color.WHITE);
        interfaz.stage().draw();
    }

    @Override
    public void resize(int width, int height) {
        interfaz.stage().getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (interfaz != null) {
            interfaz.dispose();
        }
    }
}
