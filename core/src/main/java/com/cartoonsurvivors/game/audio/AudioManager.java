package com.cartoonsurvivors.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class AudioManager {

    private Music musicaMenu;
    private Music musicaJuego;

    public void cargarAudio() {
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("audio/musica/musicaMenu.wav"));
        musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("audio/musica/musicaJuego.wav"));
    }

    public void reproducirMusicaMenu() {
        if (!musicaMenu.isPlaying()) {
            musicaMenu.setLooping(true);
            musicaMenu.setVolume(0.5f);
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
            musicaJuego.setVolume(0.5f);
            musicaJuego.play();
        }
    }

    public void detenerMusicaJuego() {
        if (musicaJuego.isPlaying()) {
            musicaJuego.stop();
        }
    }

    public void dispose() {
        musicaMenu.dispose();
        musicaJuego.dispose();
    }
}
