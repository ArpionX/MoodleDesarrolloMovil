# Roadmap — Proyecto Integrador Moodle + Android

Estado del alcance obligatorio del PDF (`Proyecto de fin de curso.pdf`, sección 5.1). Las 5 pantallas obligatorias están implementadas siguiendo el mismo patrón de arquitectura por capas.

## Estado actual

| Objetivo / Pantalla | Estado |
|---|---|
| O1. Moodle instalado + web services REST habilitados | ✅ Hecho (VM VirtualBox) |
| O2. Probar/consumir endpoints REST | ✅ Hecho — endpoints de login, cursos, actividades, tareas y foros integrados |
| O3. OAuth 2.0 desde la app móvil | ✅ Completo — Credential Manager + validación de correo contra Moodle, verificado en dispositivo real |
| 1. Pantalla de bienvenida y autenticación | ✅ Completo (`LoginActivity`) |
| 2. Lista de cursos | ✅ Completo (`CoursesActivity`) |
| 3. Actividades del curso | ✅ Completo (`CourseDetailActivity`) |
| 4. Envío de tareas | ✅ Completo, solo texto (`AssignmentDetailActivity`) — adjunto de archivos vía `core_files_upload` fuera de alcance por decisión de proyecto |
| 5. Interacción con foros | ✅ Completo (`ForumDiscussionsActivity`, `ForumPostsActivity`) |
| Opcional: push (Firebase), Room/offline | ⬜ No iniciado (10% de la nota, C5) |
| Informe técnico | ⬜ No iniciado |
| Video demostrativo | ⬜ No iniciado |

## Arquitectura implementada

```
app/src/main/java/ec/edu/ug/proyectodesarrollomovil/
├── auth/                          # Credential Manager (Google OAuth 2.0)
├── data/
│   ├── remote/
│   │   ├── MoodleApi.kt           # interfaz Retrofit — un endpoint @GET/@POST por función Moodle
│   │   ├── MoodleApiClient.kt     # singleton Retrofit/OkHttp con DNS override + parseo manual JSON
│   │   └── dto/                   # CourseDto, ModuleDto, AssignmentDto, ForumDiscussionDto, ForumPostDto, MoodleUserDto
│   ├── local/
│   │   └── SecureTokenStorage.kt  # EncryptedSharedPreferences: wstoken + userId
│   └── repository/                # AuthRepository, CourseRepository, CourseContentRepository, ForumRepository, AssignmentRepository
└── ui/
    ├── login/                     # LoginActivity + LoginViewModel
    ├── courses/                   # CoursesActivity + CoursesViewModel + CourseAdapter
    ├── coursedetail/               # CourseDetailActivity + CourseDetailViewModel + ModuleAdapter
    ├── assignment/                 # AssignmentDetailActivity + AssignmentDetailViewModel
    └── forum/                      # ForumDiscussionsActivity + ForumPostsActivity + ViewModels + adapters
```

**Patrón replicado en cada pantalla** (establecido originalmente en el login):
- `MoodleApi.kt` expone cada función Moodle devolviendo `ResponseBody` crudo — nunca un DTO tipado directo, porque Moodle responde un objeto `{exception, errorcode, message}` en error en lugar del array/objeto esperado.
- `MoodleApiClient.kt` parsea esa respuesta a mano con `org.json` hacia el DTO correspondiente, lanzando `MoodleApiException` en caso de error.
- Cada `Repository` envuelve las llamadas en `withContext(Dispatchers.IO)`.
- Cada `ViewModel` es `AndroidViewModel` con `sealed class UiState` (`Idle`/`Loading`/`Success`/`Failed`) expuesto vía `StateFlow`.
- Cada `Activity` usa `findViewById` (sin ViewBinding), observa el `StateFlow` con `flowWithLifecycle(STARTED)`, y navega vía `Intent` explícito + companion `newIntent(...)`.
- Las escrituras (`mod_forum_add_discussion_post`, `mod_assign_save_submission`) usan `@POST` + `@FormUrlEncoded` en vez de `@GET`, para evitar límites de longitud de URL con mensajes largos.

**Recordatorio importante** (aprendido a las malas con `core_user_get_users_by_field`): cada función nueva de Moodle que se llame **hay que habilitarla manualmente** en Moodle admin → Administración del sitio → Servidor → Servicios web → Servicios externos → [servicio del token] → Funciones. Si tira "Excepción al control de acceso", es casi siempre eso, no un bug de código.

## Detalle por pantalla

**Pantalla 2 — Cursos:** `core_enrol_get_users_courses` (userid) + `core_enrol_get_enrolled_users` (courseid, por curso) para resolver el nombre del docente filtrando por rol `editingteacher`/`teacher`.

**Pantalla 3 — Actividades:** `core_course_get_contents` (courseid) → secciones aplanadas en una lista de `ModuleDto` (sección denormalizada en cada módulo). Click en `modname == "assign"` navega a Pantalla 4 pasando `courseId` + `cmid`; `modname == "forum"` navega a Pantalla 5 pasando el `instance` (forumid) del módulo.

**Pantalla 4 — Tareas (solo texto):** `mod_assign_get_assignments` (courseids[]) filtrando por `cmid` para resolver el `assignmentid`, luego `mod_assign_save_submission` con `plugindata[onlinetext_editor][text]`.

**Pantalla 5 — Foros:** `mod_forum_get_forum_discussions` (forumid) → `mod_forum_get_forum_discussion_posts` (discussionid) → `mod_forum_add_discussion_post` respondiendo siempre al primer post cargado de la discusión (`postid` = id del post raíz).

## Opcional (C5 — 10% de la nota, hacer solo si sobra tiempo)

- **Room** para cachear cursos/actividades localmente (`data/local/` — agregar entidades y DAO junto a `SecureTokenStorage`).
- **Firebase push**: requiere `google-services.json` (no existe todavía) + proyecto Firebase nuevo — evaluar si vale la pena dado que ya está el alcance obligatorio completo.

## Entregables pendientes (no son código)

- **Informe técnico**: portada, introducción/objetivos, arquitectura implementada (usar este roadmap como base), instalación/configuración de Moodle (documentar el setup de VM + VirtualBox + DNS override), documentación de pantallas (capturas), casos de prueba, conclusiones.
- **Video demo** (5-8 min): mostrar login, cursos, actividades, envío de tarea, foro.
- Verificar cada función Moodle habilitada en el servidor y probar el flujo completo en dispositivo real antes de grabar el video.
