package com.cartoonsurvivors.game.entidades.enemigos;

import com.badlogic.gdx.graphics.Texture;

public class EnemigoSpawner {

    private int vida;
    private float velocidad;
    private int danio;
    private Texture spriteSheet;

    public EnemigoSpawner(int vida, float velocidad, int danio, Texture spriteSheet) {
        this.vida = vida;
        this.velocidad = velocidad;
        this.danio = danio;
        this.spriteSheet = spriteSheet;
    }

    public EnemigoBasico spawn(float posicionX, float posicionY) {
        return new EnemigoBasico(vida, posicionX, posicionY, velocidad, danio, spriteSheet);
    }

}
