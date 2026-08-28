# Changelog

Todos los cambios importantes del proyecto se documentan en este archivo.

## [Unreleased]

### Agregado

* Pendiente.

---

## [0.1.9]

### Agregado

* Se agregó el mapa visible dentro del juego.

---

## [0.1.8]

### Agregado

* la camara ahora sigue al jugador.
* Se agregó la clase aparte para los controles del jugador.
* Se implementó el movimiento del jugador en diagonal y que puedan modificarse ambas dimensiones (x, y) a la vez.

---

## [0.1.7] - 2026-08-27

### Modificado

* Se modificó las entidades en sí para que sea cada quien controla el sprite de si mismo.
* se agregó el uso de las constantes para el jugador
* se modificó el archivo para que no tenga vsync y esté capeado a 60 fps.

---

## [0.1.6] - 2026-08-27

### Modificado

* Se creo al jugador visible en la pantalla de juego.
* Un metodo cavernícola de mover al player con las teclas de direccion.

---

## [0.1.5] - 2026-08-11

### Agregado

* Implementación del menú principal del juego.
* Incorporación del fondo visual del menú principal.
* Integración de `MenuPrincipal` como pantalla inicial del juego.

### Modificado

* Se reemplazó la pantalla de controles inicial por el menú principal.
* Se ajustó la configuración de la fuente utilizada por la interfaz.
* Se adaptó la pantalla principal para utilizar el fondo ocupando el espacio del mundo del juego.
* Se modificó la clase `Constantes` para simplificar su constructor privado.

---

## [0.1.4] - 2026-08-11

### Agregado

* Implementación de la pantalla básica base para la interfaz del juego en los módulos `core` y `lwjgl3`.
* Carga e integración de una fuente de letra personalizada en la carpeta `assets` para el renderizado de texto y HUD.

---

## [0.1.3] - 2026-08-11

### Modificado

* Se reorganizó la Wiki para reflejar la estructura de la propuesta formal.
* Se incorporaron las secciones de alcance esperado y alcance deseable.
* Se eliminaron textos de plantilla e información provisoria de la Wiki.
* Se actualizaron las imágenes y la navegación entre páginas.

---

## [0.1.2] - 2026-08-11

### Modificado

* Se corrigió la documentación del README.
* Se unificó la versión de Java a Java 21 y JDK 21.
* Se actualizaron las instrucciones de clonación con la URL real del repositorio.
* Se corrigieron las instrucciones de ejecución para el módulo lwjgl3.
* Se agregó una sección con el estado actual del proyecto.

---

## [0.1.1] - 2026-08-11

### Modificado

* Se renombró correctamente el archivo CHANGELOG.md.
* Se reorganizó el historial inicial de cambios.

---

## [0.1.0] - 2026-07-17

### Agregado

* Inicialización del proyecto con LibGDX mediante gdx-liftoff.
* Configuración inicial del proyecto con Gradle.
* Creación de la estructura base del juego.
* Configuración del archivo .gitignore.
* Creación del archivo README.md.
* Creación del archivo CHANGELOG.md.
* Publicación de la propuesta formal del proyecto en la Wiki del repositorio.
