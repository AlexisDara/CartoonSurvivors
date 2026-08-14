# Cartoon Survivors

## Integrantes del Grupo

- Juan Pablo Ali Cigno
- Valentino Cullari
- Alexis Daraio

## Descripción

Los jugadores deberán enfrentarse a oleadas continuas de enemigos e intentar sobrevivir durante un tiempo determinado. Una vez alcanzado ese límite, los jugadores serán eliminados por un enemigo invencible que marcará el fin de la partida. Esto no significa que pierdan, sino que lograron pasar el escenario, desbloqueando así el siguiente.
A medida que avance la partida, la dificultad aumentará progresivamente mediante la mejora de las estadísticas enemigas y el incremento de la cantidad de apariciones, obligando a los jugadores a adaptar constantemente sus estrategias para mantenerse con vida. Para lograr sobrevivir los jugadores contaran con armas y objetos pasivos que deberan de subir de nivel mediante la obtención de oro y experiencia.


## Tecnologías Principales

- **Lenguaje:** Java 21.
- **Framework:** LibGDX versión 1.14.2.
- **Control de versiones:** Git y GitHub.
- **Herramientas de organización:** Trello.
- **Editor de mapas:** Tiled.

## Estado actual del proyecto

Actualmente el proyecto se encuentra en una **etapa inicial de desarrollo**.

### Implementado

- Estructura base del proyecto con LibGDX.
- Sistema de pantallas.
- Fondo visual del menú principal.
- Integración de una fuente personalizada para la interfaz.

### En desarrollo

- Menú principal.
- Movimiento del jugador.
- Sistema de enemigos.
- HUD.
- Sistema de combate.
- Sistema de experiencia y niveles.

## Wiki del Proyecto

La documentación completa del proyecto, incluyendo la propuesta formal, los alcances, la planificación y el diseño general del juego, se encuentra disponible en la Wiki del repositorio.

**<https://github.com/AlexisDara/CartoonSurvivors/wiki>**

## Compilación y Ejecución

### Requisitos

- **Java JDK 21**.
- Git.
- Gradle (opcional, ya que el proyecto incluye el **Gradle Wrapper**).

### Clonar el repositorio

```bash
git clone https://github.com/AlexisDara/CartoonSurvivors.git
cd CartoonSurvivors
```

### Ejecutar el proyecto (LWJGL3)

En Windows:

```bash
gradlew.bat lwjgl3:run
```

En Linux/macOS:

```bash
./gradlew lwjgl3:run
```

### Compilar el proyecto

```bash
./gradlew build
```

o en Windows:

```bash
gradlew.bat build
```
