package com.cartoonsurvivors.game.utilidades;

public enum TipoMapa {
    MAPA1("Tierra", "mapas/mapa1/mapa1.tmx"),
    MAPA2("Marte", "mapas/mapa2/mapa2.tmx");

    private final String nombre;
    private final String ruta;

    TipoMapa(String nombre, String ruta) {
        this.nombre = nombre;
        this.ruta = ruta;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRuta() {
        return ruta;
    }

    public static TipoMapa desdeId(String id) {
        for (TipoMapa mapa : values()) {
            if (mapa.name().equals(id)) {
                return mapa;
            }
        }
        return null;
    }
}
