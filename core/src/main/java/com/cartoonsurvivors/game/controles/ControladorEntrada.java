package com.cartoonsurvivors.game.controles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

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

        public boolean pasarPantalla() {
            return Gdx.input.isKeyPressed(Input.Keys.SPACE);
        }

}
