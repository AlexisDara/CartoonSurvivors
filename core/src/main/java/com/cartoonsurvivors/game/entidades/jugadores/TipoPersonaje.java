package com.cartoonsurvivors.game.entidades.jugadores;

public enum TipoPersonaje {

    MORDECAI("MORDECAI");

    private final String nombre;

    TipoPersonaje(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Jugador crearPersonaje() {

        switch (this) {
            case MORDECAI:
                return new Mordecai();
            default:
                throw new IllegalArgumentException("Personaje no reconocido");
        }
    }
}
