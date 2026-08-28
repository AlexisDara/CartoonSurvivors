package com.cartoonsurvivors.game.utilidades;

public final class Constantes {

    // Constructor privado para evitar que la clase sea instanciada
    private Constantes() {}

    // Configuración General del Mundo y Pantalla (16:9)
    public static final class Mundo {
        public static final float ANCHO_MUNDO = 1280f;
        public static final float ALTO_MUNDO = 720f;

        // Píxeles virtuales para la cámara del HUD / Interfaz
        public static final float ANCHO_INTERFAZ = 1280f;
        public static final float ALTO_INTERFAZ = 720f;
    }

    // Atributos y Balance del Jugador
    public static final class Jugador {
        public static final int vidaInicial = 100;
        public static final int velocidadInicial = 1500;
    }

    // Atributos y Balance de Enemigos
    public static final class Enemigos {
        public static final float VELOCIDAD_SLIME = 2f;
        public static final float TIEMPO_APARICION = 1.5f; // Cada cuántos segundos aparece un enemigo
        public static final float MARGEN_APARICION = 2f;    // Distancia fuera del área visible de la cámara
    }

    // Rutas de Archivos (Assets)
    public static final class Recursos {
        public static final String RUTA_FUENTE = "CherryCreamSoda-Regular.ttf";

    }
}
