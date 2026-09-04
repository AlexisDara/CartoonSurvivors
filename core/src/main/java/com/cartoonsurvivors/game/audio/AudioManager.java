package com.cartoonsurvivors.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;

public class AudioManager {

    private Music musicaMenu;
    private Music musicaJuego;

    private float volumenMusica = 0.1f;
    private float volumenSonidoEfecto = 0.1f;

    private int volumenMuscaporcentaje = 50;

    private Preferences prefs = Gdx.app.getPreferences("configuracion");


    public void setVolumenMusica(float volumenMusica) {

        this.volumenMusica = Math.max(0f, Math.min(1f, volumenMusica));
        prefs.putFloat("volumenMusica",this.volumenMusica);
        prefs.flush();

        if (musicaMenu != null) {
            musicaMenu.setVolume(this.volumenMusica);
        }
        if (musicaJuego != null) {
            musicaJuego.setVolume(this.volumenMusica);
        }

    }

    public void cargarAudio() {
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("audio/musica/musicaMenu.wav"));
        musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("audio/musica/musicaJuego.wav"));

        volumenMusica = prefs.getFloat("volumenMusica", volumenMusica);
        volumenSonidoEfecto = prefs.getFloat("volumenSonidoEfecto", volumenSonidoEfecto);
    }

    public void reproducirMusicaMenu() {
        if (!musicaMenu.isPlaying()) {
            musicaMenu.setLooping(true);
            musicaMenu.setVolume(volumenMusica);
            musicaMenu.play();
        }
    }

    public void detenerMusicaMenu() {
        if (musicaMenu.isPlaying()) {
            musicaMenu.stop();
        }
    }

    public void reproducirMusicaJuego() {
        if (!musicaJuego.isPlaying()) {
            musicaJuego.setLooping(true);
            musicaJuego.setVolume(volumenMusica);
            musicaJuego.play();
        }
    }

    public void detenerMusicaJuego() {
        if (musicaJuego.isPlaying()) {
            musicaJuego.stop();
        }
    }

    public float getVolumenMusica() {
        return Math.max(0f, Math.min(1f, prefs.getFloat("volumenMusica", 0.1f)));
    }

    public void setVolumenSonidoEfecto(float volumenSonidoEfecto) {
        this.volumenSonidoEfecto = Math.max(0f, Math.min(1f, volumenSonidoEfecto));
        prefs.putFloat("volumenSonidoEfecto", this.volumenSonidoEfecto);
        prefs.flush();
    }

    public float getVolumenSonidoEfecto() {
        return volumenSonidoEfecto;
    }



    public void dispose() {
        if (musicaMenu != null) {
            musicaMenu.dispose();
        }
        if (musicaJuego != null) {
            musicaJuego.dispose();
        }
    }
}
