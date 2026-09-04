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
        public static final int VIDA_INICIAL = 100;
        public static final float VELOCIDAD_INICIAL = 200f;
        public static final int TAMAÑO_SPRITE = 64;
        public static final float TAMAÑO_REAL = 100f;
    }

    // Atributos y Balance de Enemigos
    public static final class Enemigos {
        public static final float VELOCIDAD_ENEMIGO = 80f;
        public static final float TIEMPO_APARICION = 1.5f; // Cada cuántos segundos aparece un enemigo
        public static final float MARGEN_APARICION = 2f;
        public static final float ANCHO_ENEMIGO = 36f; // Ancho del sprite del enemigo
        public static final float ALTO_ENEMIGO = 60f; // Alto del sprite del enemigo
        public static final int VIDA_ENEMIGO = 20;
        public static final int DANIO_ENEMIGO = 15;
    }

    // Rutas de Archivos (Assets)
    public static final class Recursos {
        public static final String RUTA_FUENTE = "CherryCreamSoda-Regular.ttf";

    }
}
