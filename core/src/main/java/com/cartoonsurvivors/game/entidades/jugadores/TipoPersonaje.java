package com.cartoonsurvivors.game.entidades.jugadores;

public enum TipoPersonaje {

    MORDECAI("jugador/mordecai-idle.png"),
    FINN("jugador/FinIdle.png");

    private final String rutaIdle;

    TipoPersonaje(String rutaIdle) {
        this.rutaIdle = rutaIdle;
    }

    public String getNombre() {
        return name();
    }

    public String getRutaIdle() {
        return rutaIdle;
    }

    public Jugador crearPersonaje() {
        return switch (this) {
            case MORDECAI -> new Mordecai();
            case FINN -> new Finn();
        };
    }
}
