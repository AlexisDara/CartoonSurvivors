package com.cartoonsurvivors.game.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entidad {
    private int vida;
    private float velocidad;
    private int danio;
    private Rectangle hitbox;

    // Posición y tamaño del sprite (lo que se dibuja)
    protected float spriteX;
    protected float spriteY;
    protected float spriteWidth;
    protected float spriteHeight;

    // Offset de la hitbox respecto al sprite (calculado)
    private float hitboxOffsetX = 0f;
    private float hitboxOffsetY = 0f;

    protected Entidad(int vida, float velocidad, int danio, float posicionX, float posicionY, float altura, float ancho) {
        this.vida = vida;
        this.velocidad = velocidad;
        this.danio = danio;
        // hitbox: width = ancho, height = altura (constructor original)
        this.hitbox = new Rectangle(posicionX, posicionY, ancho, altura);
        // Inicialmente, asumimos que el sprite tiene el mismo tamaño que la hitbox.
        this.spriteX = posicionX;
        this.spriteY = posicionY;
        this.spriteWidth = ancho;
        this.spriteHeight = altura;
        this.hitboxOffsetX = (this.spriteWidth - this.hitbox.width) / 2f;
        this.hitboxOffsetY = (this.spriteHeight - this.hitbox.height) / 2f;
        // Asegurar que la hitbox esté correctamente ubicada respecto al sprite
        this.hitbox.setPosition(this.spriteX + hitboxOffsetX, this.spriteY + hitboxOffsetY);
    }

    public void mover(float direccionX, float direccionY, float delta) {
        float movimientoX = direccionX * delta * this.velocidad;
        float movimientoY = direccionY * delta * this.velocidad;
        // mover la posición del sprite y recalcular la posición de la hitbox
        this.spriteX += movimientoX;
        this.spriteY += movimientoY;
        this.hitbox.setPosition(this.spriteX + hitboxOffsetX, this.spriteY + hitboxOffsetY);
    }

    // getPosicion* deben devolver la posición del sprite (usada para dibujar y cámara)
    public float getPosicionX() {
        return spriteX;
    }
    public float getPosicionY() {
        return spriteY;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public void setPosicion(float posicionX, float posicionY) {
        this.spriteX = posicionX;
        this.spriteY = posicionY;
        this.hitbox.setPosition(this.spriteX + hitboxOffsetX, this.spriteY + hitboxOffsetY);
    }

    public int getVida() {
        return vida;
    }

    public int getDanio() {
        return danio;
    }

    public void recibirDanio(int danio) {
        vida -= danio;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public boolean colisionaCon(Entidad entidad) {
        return hitbox.overlaps(entidad.hitbox);
    }

    public abstract void dibujar(Batch batch, float delta);

    /**
     * Establece el tamaño real del sprite usado para dibujar esta entidad.
     * Esto recalcula el offset de la hitbox para que quede centrada respecto al sprite.
     */
    protected void setSpriteSize(float width, float height) {
        this.spriteWidth = width;
        this.spriteHeight = height;
        this.hitboxOffsetX = (this.spriteWidth - this.hitbox.width) / 2f;
        this.hitboxOffsetY = (this.spriteHeight - this.hitbox.height) / 2f;
        // Reposicionar la hitbox según la nueva configuración
        this.hitbox.setPosition(this.spriteX + hitboxOffsetX, this.spriteY + hitboxOffsetY);
    }

    public com.badlogic.gdx.math.Rectangle getHitbox() {
        return hitbox;
    }

}