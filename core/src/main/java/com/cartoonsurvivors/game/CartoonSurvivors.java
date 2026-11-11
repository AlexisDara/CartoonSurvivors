package com.cartoonsurvivors.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.cartoonsurvivors.game.pantallas.JuegoPantalla;
import com.cartoonsurvivors.game.pantallas.MenuPantalla;
import static com.cartoonsurvivors.game.utilidades.Constantes.Recursos.*;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.red.ClienteUDP;

public class CartoonSurvivors extends Game {
    private SpriteBatch batch;
    private BitmapFont font;
    private AudioManager audioManager;
    private ClienteUDP clienteUDP;
    @Override
    public void setScreen(Screen nuevaPantalla) {
        Screen pantallaAnterior = getScreen();

        super.setScreen(nuevaPantalla);

        if (pantallaAnterior != null && pantallaAnterior != nuevaPantalla) {
            pantallaAnterior.dispose();
        }
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        audioManager = new AudioManager();
        audioManager.cargarAudio();

        var generator = new FreeTypeFontGenerator(Gdx.files.internal(RUTA_FUENTE));
        var parametrosLetra = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parametrosLetra.size = 40;
        parametrosLetra.color = Color.WHITE;


        parametrosLetra.borderWidth = 3f;
        parametrosLetra.borderColor = Color.BLACK;


        parametrosLetra.shadowOffsetX = 2;
        parametrosLetra.shadowOffsetY = 2;
        parametrosLetra.shadowColor = new Color(0f, 0f, 0f, 0.5f);

        font = generator.generateFont(parametrosLetra);
        generator.dispose();

        clienteUDP = new ClienteUDP();
        clienteUDP.conectar();
        setScreen(new MenuPantalla(this));
    }

    @Override
    public void render() {
        super.render();
        if (clienteUDP != null) {
            clienteUDP.actualizar();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();
        audioManager.dispose();
        if (clienteUDP != null) {
            clienteUDP.desconectar();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public AudioManager getAudioManager() { return audioManager; }
}
