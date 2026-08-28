package com.cartoonsurvivors.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class AudioManager {

    private Music musicaMenu;

    public void cargarAudio() {
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/musicaMenu.wav"));
    }

    public void reproducirMusica() {
        if (!musicaMenu.isPlaying()) {
            musicaMenu.setLooping(true);
            musicaMenu.setVolume(0.5f);
            musicaMenu.play();
        }
    }

    public void detenerMusica() {
        if (musicaMenu.isPlaying()) {
            musicaMenu.stop();
        }
    }
}
