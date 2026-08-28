package com.cartoonsurvivors.game.controles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class ControladorEntrada {


        public float obtenerDireccionX() {
            float direccionX = 0;

            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                direccionX--;
                System.out.println("A presionada");
            }

            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                direccionX++;
                System.out.println("D presionada");

            }

            return direccionX;
        }

        public float obtenerDireccionY() {
            float direccionY = 0;

            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                direccionY--;
                System.out.println("S presionada");

            }

            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                direccionY++;
                System.out.println("W presionada");
            }

            return direccionY;
        }

}
