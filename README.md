# Cartoon Survivors

## Integrantes del Grupo

- Juan Pablo Ali Cigno
- Valentino Cullari
- Alexis Daraio

## Descripción

Los jugadores deberán enfrentarse a oleadas continuas de enemigos e intentar sobrevivir durante un tiempo determinado. Una vez alcanzado ese límite, los jugadores serán eliminados por un enemigo invencible que marcará el fin de la partida. Esto no significa que pierdan, sino que lograron pasar el escenario, desbloqueando así el siguiente.

A medida que avance la partida, la dificultad aumentará progresivamente mediante la mejora de las estadísticas enemigas y el incremento de la cantidad de apariciones, obligando a los jugadores a adaptar constantemente sus estrategias para mantenerse con vida. Para lograr sobrevivir los jugadores contarán con armas y objetos pasivos que deberán subir de nivel mediante la obtención de oro y experiencia.

## Tecnologías Principales

- **Lenguaje:** Java 21.
- **Framework:** LibGDX versión 1.14.2.
- **Control de versiones:** Git y GitHub.
- **Herramientas de organización:** Trello.
- **Editor de mapas:** Tiled.

## Estado actual del proyecto

Actualmente el proyecto cuenta con un **prototipo jugable**, en el cual ya se encuentran implementadas las mecánicas principales de movimiento, enemigos, combate, HUD y condiciones de victoria y derrota.

### Implementado

- Estructura base del proyecto con LibGDX.
- Sistema de pantallas.
- Menú principal.
- Pantalla de controles.
- Pantalla de pausa.
- Fondo visual del menú principal.
- Integración de una fuente personalizada para la interfaz.
- Mapa creado con Tiled.
- Cámara que sigue al jugador.
- Viewport independiente para el mundo y el HUD.
- Personaje jugable Mordecai.
- Movimiento del jugador mediante teclado.
- Animación de movimiento mediante spritesheet.
- Sistema de enemigos.
- Spawner de enemigos.
- Enemigos que persiguen al jugador.
- Sistema de vida y daño.
- Sistema de combate cuerpo a cuerpo.
- Arma espada.
- Detección de impactos mediante hitboxes.
- Eliminación de enemigos al perder toda su vida.
- Contador de enemigos eliminados.
- HUD con vida, tiempo de supervivencia y enemigos eliminados.
- Música para el menú y la partida.
- Sistema de pausa y reanudación.
- Reinicio de la partida.
- Pantalla de derrota con estadísticas de la partida.
- Pantalla de victoria al alcanzar el tiempo de supervivencia establecido.

### En desarrollo

- Sistema de experiencia.
- Sistema de niveles.
- Barra de experiencia.
- Sistema de recompensas al subir de nivel.
- Mejoras de estadísticas del jugador.
- Sistema de oro.
- Objetos pasivos.
- Incorporación de nuevas armas.
- Incorporación de nuevos personajes jugables.
- Nuevos tipos de enemigos.
- Aumento progresivo de la dificultad.
- Efectos de sonido.
- Control de volumen y silencio.
- Mejoras visuales y de interfaz.
- Incorporación de nuevos escenarios y contenido.

## Wiki del Proyecto

La documentación completa del proyecto, incluyendo la propuesta formal, los alcances, la planificación y el diseño general del juego, se encuentra disponible en la Wiki del repositorio.

**https://github.com/AlexisDara/CartoonSurvivors/wiki**

## Trello del proyecto

Tablero dónde se podrá ir actualizando el progreso del proyecto.

**https://trello.com/invite/b/6a844123753463726cf39e4a/ATTIe62c33bef3b8297f15e1d74286ac0043A0D807D1/cartoonsurvivors**

## Compilación y Ejecución

### Requisitos

- **Java JDK 21**.
- Git.
- Gradle (opcional, ya que el proyecto incluye el **Gradle Wrapper**).

### Clonar el repositorio

```bash
git clone https://github.com/AlexisDara/CartoonSurvivors.git
cd CartoonSurvivors
