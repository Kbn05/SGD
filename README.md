# SGD UPB - Sistema de Gestión de Documentos

Aplicación de escritorio desarrollada en Java para gestionar archivos en una estructura de carpetas local. Permite subir, descargar, eliminar y ver propiedades de archivos, además de manejar notificaciones en tiempo real mediante sockets y acceder a versiones de archivos mediante RMI.

## Características

- 🌳 Visualización de directorios con `JTree`
- 📂 Listado y renderizado personalizado de archivos
- 📤 Subida y descarga de archivos
- 🔄 Renombrar y eliminar archivos
- 📑 Mostrar propiedades de archivos (incluyendo permisos)
- 🛎️ Sistema de notificaciones en tiempo real
- 🔍 Búsqueda de archivos por nombre, tipo o fecha
- 🧠 Comunicación por Sockets y RMI
- 💬 Interfaz gráfica interactiva construida con Java Swing

## Tecnologías utilizadas

- Java SE 11+
- Java Swing
- Sockets (para recibir notificaciones)
- Java RMI (para gestión remota de archivos)

## Estructura del proyecto
```
├── Main.java # Clase principal con la interfaz gráfica
├── client/
│ └── Client.java # Lógica de cliente para sockets
├── socket/
│ └── JavaClientSocket.java # Configuración de socket
├── sgd.repository/
│ └── FileManager.java # Interfaz RMI para manejo de archivos
└── resources/
├── bell.png
├── folder.png
├── file.png
├── upload.png
└── version.png
```

## Requisitos

- JDK 11 o superior
- Servidor RMI ejecutando la interfaz `FileManager`
- Servidor de notificaciones por socket (puerto 5055 por defecto)
- Imágenes de recursos ubicadas en `resources/`

## Ejecución

1. Asegúrate de que el servidor RMI esté activo en `localhost:5056` y que el socket esté escuchando en `localhost:5055`.
2. Compila y ejecuta el proyecto desde tu IDE o terminal:

```bash
javac Main.java
java Main
```
Al iniciar la aplicación:

Se conecta al servidor de notificaciones vía socket.

Carga la estructura de carpetas desde el directorio base (Deberás definirlo en la variable estática `Path`).

Muestra un panel con opciones para buscar archivos y recibir notificaciones.
