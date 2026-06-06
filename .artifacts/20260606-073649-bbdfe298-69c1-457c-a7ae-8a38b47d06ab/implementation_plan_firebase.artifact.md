# Plan de Implementación - Integración de Firebase y Sincronización Cloud

Este plan describe los pasos necesarios para habilitar la sincronización en la nube y las funcionalidades colaborativas (pareja/familia) en AppCasa utilizando el ecosistema Firebase.

## 1. Configuración de Infraestructura (Alta Prioridad)

- **Objetivo:** Activar los servicios de Firebase en el proyecto Android.
- **Acciones:**
    - Descomentar el plugin `google-services` en `app/build.gradle.kts`.
    - Añadir el classpath de Google Services en el `build.gradle.kts` raíz (si falta).
    - **Nota:** El usuario deberá descargar y colocar el archivo `google-services.json` en la carpeta `app/`.

## 2. Autenticación y Perfiles (Media Prioridad)

- **Objetivo:** Permitir que los miembros del hogar se identifiquen para sincronizar sus datos.
- **Acciones:**
    - Configurar **Firebase Auth** (Sugerido: Google Sign-In o Email/Password).
    - Vincular el `User` local (Room) con el `FirebaseUser` (UID).
    - Actualizar `UserRepository` para manejar el login/logout remoto.

## 3. Capa de Datos Remota en `:core:data` (Alta Prioridad)

- **Objetivo:** Crear el puente entre la App y la nube.
- **Acciones:**
    - Crear `FirestoreDataSource` en `:core:data`.
    - Implementar lógica de lectura/escritura para las colecciones críticas:
        - `households/` (Datos del hogar y código de invitación).
        - `tasks/` (Tareas compartidas).
        - `expenses/` (Gastos comunes).
        - `members/` (Estado de ánimo y perfiles sincronizados).
    - Implementar un patrón **Offline-First**:
        - La App siempre escribe en Room primero.
        - Un `SyncWorker` (WorkManager) sube los cambios pendientes a Firestore.
        - Firestore `Snapshots` actualizan Room en tiempo real cuando hay cambios remotos.

## 4. Notificaciones Push y Eventos (Media Prioridad)

- **Objetivo:** Avisar instantáneamente cuando la pareja añade un gasto o completa una tarea.
- **Acciones:**
    - Implementar `AppFirebaseMessagingService` en `:core:data` o `:app`.
    - Suscribir el dispositivo al "Topic" del ID del Hogar (`household_{id}`).
    - Configurar Cloud Functions (o triggers simples) para enviar notificaciones cuando cambie una colección en Firestore.

## 5. Migración de UseCases (Baja Prioridad)

- **Objetivo:** Asegurar que la lógica de negocio no cambie, solo la fuente de datos.
- **Acciones:**
    - No se requieren cambios en los UseCases (gracias a la arquitectura actual), ya que estos dependen de interfaces de Repositorio que simplemente ahora inyectarán datos combinados (Local + Remoto).

---

## Plan de Verificación

### Pruebas de Sincronización
1.  **Modo Avión:** Crear una tarea sin conexión. Verificar que se guarda en Room.
2.  **Reconexión:** Activar internet. Verificar que el `SyncWorker` sube la tarea a Firebase.
3.  **Multi-dispositivo:** Usar dos emuladores. Cambiar el estado de ánimo en uno y verificar que el emoji cambia en el otro en < 2 segundos.

### Seguridad
- Verificar que las reglas de seguridad de Firestore impiden que un usuario de un hogar vea datos de otro hogar (filtrado por `hogarId`).
