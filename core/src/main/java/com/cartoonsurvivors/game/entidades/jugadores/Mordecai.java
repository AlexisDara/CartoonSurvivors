package com.cartoonsurvivors.game.entidades.jugadores;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cartoonsurvivors.game.utilidades.Constantes;

public class Mordecai extends Jugador{

    public Mordecai() {
        super(new Texture("jugador/mordecai-idle.png"), crearAnimacionCaminar());
    }


    private static Animation<TextureRegion> crearAnimacionCaminar() {
        Texture spritesheet = new Texture("jugador/mordecai-caminando-SpriteSheet.png");

        TextureRegion[][] frames = TextureRegion.split(spritesheet, Constantes.Jugador.TAMAÑO_SPRITE, Constantes.Jugador.TAMAÑO_SPRITE);

        TextureRegion[] framesCaminar = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            framesCaminar[i] = frames[0][i];
        }

        return new Animation<>(0.1f, framesCaminar);
    }
}
