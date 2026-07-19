# Proyecto Integrador — Cliente Móvil para Moodle

Aplicación Android nativa (Kotlin) que consume la API REST de Moodle mediante autenticación OAuth 2.0 con Google, e implementa las 5 pantallas del alcance obligatorio: login, cursos, actividades, envío de tareas y foros.

## Requisitos previos

- Android Studio (Ladybug o superior) con SDK 36 instalado.
- JDK 11.
- Una instancia de Moodle accesible en red local (VM o XAMPP/WAMP), con:
  - Servicios web + protocolo REST habilitados.
  - Un curso de pruebas con un usuario estudiante matriculado.
  - Un token de servicio (`wstoken`) asociado a ese usuario, con las siguientes funciones habilitadas en Administración del sitio → Servidor → Servicios web → Servicios externos → [tu servicio] → Funciones:
    - `core_webservice_get_site_info`
    - `core_user_get_users_by_field`
    - `core_enrol_get_users_courses`
    - `core_enrol_get_enrolled_users`
    - `core_course_get_contents`
    - `mod_assign_get_assignments`
    - `mod_assign_save_submission`
    - `mod_forum_get_forum_discussions`
    - `mod_forum_get_forum_discussion_posts`
    - `mod_forum_add_discussion_post`
- Un proyecto en Google Cloud Console con un OAuth Client ID de tipo **Web** (usado por Credential Manager para el inicio de sesión con Google).

## Configuración

El proyecto no trae credenciales embebidas: se leen desde `local.properties` (archivo ignorado por Git, no se versiona). Creá o completá `local.properties` en la raíz del proyecto con:

```properties
MOODLE_BASE_URL=http://labdigital.com/
MOODLE_ADMIN_TOKEN=<wstoken del usuario de pruebas habilitado>
GOOGLE_WEB_CLIENT_ID=<client id de OAuth tipo Web, terminado en .apps.googleusercontent.com>
MOODLE_HOST_IP=<IP LAN de la VM/host donde corre Moodle, ej. 192.168.1.55>
```

**Por qué `MOODLE_HOST_IP` además de `MOODLE_BASE_URL`:** el hostname de `MOODLE_BASE_URL` (por ejemplo `labdigital.com`) solo está mapeado en el archivo `hosts` de la PC de desarrollo. Un celular no resuelve ese nombre por DNS público, así que la app fuerza la resolución a `MOODLE_HOST_IP` vía un `okhttp3.Dns` personalizado (ver `MoodleApiClient.kt`), mientras sigue enviando el header `Host` correcto para que Apache resuelva el vhost. Si tu Moodle corre directamente sobre una IP sin vhost por nombre, `MOODLE_BASE_URL` puede apuntar directo a esa IP y `MOODLE_HOST_IP` puede quedar vacío.

Si tu dominio/IP de Moodle no es `labdigital.com` / `192.168.1.55`, actualizá también `app/src/main/res/xml/network_security_config.xml` (permite tráfico HTTP en texto plano solo hacia esos hosts, ya que Moodle en LAN normalmente no tiene HTTPS).

## Build y ejecución

1. Abrí el proyecto en Android Studio y dejá que sincronice Gradle.
2. Conectá un dispositivo físico en la misma red LAN que el host de Moodle (Credential Manager y la resolución DNS manual funcionan mejor en dispositivo real que en emulador).
3. Ejecutá el módulo `app` (Run ▶ o `./gradlew installDebug`).
4. Para solo compilar sin instalar: `./gradlew assembleDebug`.

## Flujo de la aplicación

1. **Login** (`LoginActivity`): botón "Iniciar sesión con Google" → Credential Manager → valida que el correo de Google coincida con el correo del usuario Moodle configurado (`MOODLE_ADMIN_TOKEN`). Si coincide, guarda el `wstoken` y el `userId` de forma cifrada (`EncryptedSharedPreferences`) y navega a Cursos.
2. **Cursos** (`CoursesActivity`): lista los cursos matriculados del usuario (`core_enrol_get_users_courses`) con nombre, shortname y docente.
3. **Actividades** (`CourseDetailActivity`): al tocar un curso, lista sus tareas/foros/recursos (`core_course_get_contents`).
4. **Envío de tareas** (`AssignmentDetailActivity`): al tocar una actividad de tipo tarea, muestra la descripción y permite enviar una entrega de texto (`mod_assign_save_submission`). El adjunto de archivos no está implementado (fuera del alcance mínimo).
5. **Foros** (`ForumDiscussionsActivity` → `ForumPostsActivity`): al tocar una actividad de tipo foro, lista discusiones, permite leer los mensajes de una discusión y publicar una respuesta.

## Estructura del proyecto

Ver `ROADMAP.md` para el detalle de arquitectura por capas (`data/remote`, `data/repository`, `ui/<feature>`) y las funciones Moodle usadas por cada pantalla.

## Problemas comunes

- **"Excepción al control de acceso"**: la función de Moodle llamada no está habilitada para el servicio del token. Revisar Administración del sitio → Servidor → Servicios web → Servicios externos.
- **Timeout / conexión rechazada desde el celular**: verificar que el celular esté en la misma red LAN que `MOODLE_HOST_IP`, y que el firewall del host permita conexiones entrantes al puerto de Moodle.
- **El correo de Google no coincide con Moodle**: el usuario de prueba en Moodle debe tener el mismo correo que la cuenta de Google usada para iniciar sesión.
