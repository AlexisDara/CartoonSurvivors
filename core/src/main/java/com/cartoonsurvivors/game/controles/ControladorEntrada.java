package com.cartoonsurvivors.game.controles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.cartoonsurvivors.game.audio.AudioManager;
import com.cartoonsurvivors.game.pantallas.PantallaPausa;
import com.cartoonsurvivors.game.utilidades.EstadoJuego;

public class ControladorEntrada {


        public float obtenerDireccionX() {
            float direccionX = 0;

            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                direccionX--;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                direccionX++;
            }

            return direccionX;
        }

        public float obtenerDireccionY() {
            float direccionY = 0;

            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                direccionY--;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                direccionY++;
            }

            return direccionY;
        }


        public EstadoJuego pausarPantalla(EstadoJuego estadoJuego, PantallaPausa pantallaPausa, AudioManager audioManager) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                if (estadoJuego == EstadoJuego.JUGANDO) {
                    estadoJuego = EstadoJuego.PAUSADO;
                    Gdx.input.setInputProcessor(pantallaPausa.getStage()); // El menú captura los clics
                    audioManager.pausarMusicaJuego();
                } else if (estadoJuego == EstadoJuego.PAUSADO) {
                    estadoJuego = EstadoJuego.JUGANDO;
                    Gdx.input.setInputProcessor(null); // El juego captura las teclas de nuevo
                    audioManager.reanudarMusicaJuego();
                }
            }
            return estadoJuego;
        }

        public boolean reiniciarJuego(AudioManager audioManager) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                audioManager.detenerMusicaJuego();
                return true;
            }
            return false;
        }

        public boolean volverMenu(AudioManager audioManager) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                audioManager.detenerMusicaJuego();
                return true;
            }
            return false;
        }

        public boolean pasarPantalla() {
            return Gdx.input.isKeyPressed(Input.Keys.SPACE);
        }

}
