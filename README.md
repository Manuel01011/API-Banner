# Sistema de Gestión Académica Universitaria - API

Este proyecto es una API desarrollada en Kotlin para gestionar el app llamado Banner. Proporciona endpoints para manejar carreras, cursos, profesores, alumnos, ciclos lectivos y procesos de matrícula.


## Tecnologías utilizadas

### a. HTTPURLConnection
La API utiliza `HTTPURLConnection` para las comunicaciones HTTP entre el cliente y el servidor. Esto incluye:

- Establecimiento de conexiones seguras
- Configuración de métodos HTTP (GET, POST, PUT, DELETE)
- Manejo de códigos de respuesta
- Gestión de timeouts y parámetros de conexión

### b. GSON - JSON
Para el manejo de datos en formato JSON se implementa la librería GSON que permite:

- Serialización y deserialización de objetos Kotlin a JSON
- Mapeo automático entre modelos de datos y representación JSON
- Manejo de tipos complejos y anidados
- Configuración personalizada de conversiones
