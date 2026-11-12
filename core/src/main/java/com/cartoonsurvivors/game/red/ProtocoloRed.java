package com.cartoonsurvivors.game.red;

public class ProtocoloRed {

    // Descubrimiento del servidor
    public static final String BUSCAR_SERVIDOR = "BUSCAR_SERVIDOR";
    public static final String SERVIDOR_ENCONTRADO = "SERVIDOR_ENCONTRADO";
    public static final String SERVIDOR_LLENO = "SERVIDOR_LLENO";

    // Conexión
    public static final String CONECTAR = "CONECTAR";
    public static final String OK_JUGADOR_1 = "OK-1";
    public static final String OK_JUGADOR_2 = "OK-2";
    public static final String KEEP_ALIVE = "KEEP_ALIVE";

    // Lobby
    public static final String JUGADOR_CONECTADO = "JUGADOR_CONECTADO";
    public static final String LISTO = "LISTO";
    public static final String JUGADOR_LISTO = "JUGADOR_LISTO";
    public static final String EMPIEZA = "EMPIEZA";
    public static final String SELECCIONAR_PERSONAJE = "PERSONAJE";
    public static final String PERSONAJES_ACTUALIZADOS = "PERSONAJES";

    // Partida
    public static final String PARTIDA_FINALIZADA = "PARTIDA_FINALIZADA";

    // Finalización de sesión
    public static final String VOLVER_AL_MENU = "VOLVER_AL_MENU";
    public static final String SESION_FINALIZADA = "SESION_FINALIZADA";
    public static final String DESCONEXION_INESPERADA = "DESCONEXION_INESPERADA";

    private ProtocoloRed() {
        // Evita crear instancias de esta clase.
    }
}
