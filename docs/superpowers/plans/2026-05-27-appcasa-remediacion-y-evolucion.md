# Plan de Estabilización y Evolución de AppCasa

Este plan se centra en profesionalizar la aplicación resolviendo riesgos técnicos, mejorando la arquitectura de sesión y elevando la calidad de la experiencia de usuario.

**Objetivo:** Implementar migraciones seguras, eliminar IDs fijos, asegurar recordatorios fiables y unificar la UX.

---

## 🛠️ Alcance del Plan

El trabajo se divide en 4 áreas críticas:

1.  **Estabilidad de Datos (Migraciones):** Evitar la pérdida de datos al actualizar el esquema de Room.
2.  **Arquitectura de Sesión (Adiós al `1L`):** Implementar un proveedor dinámico de contexto de hogar.
3.  **Fiabilidad de Recordatorios:** Asegurar que las notificaciones funcionen siempre, incluso tras reiniciar el móvil.
4.  **Consistencia UX/UI:** Estados vacíos, validaciones robustas y feedback visual profesional.

---

## 📋 Tareas de Implementación

### Tarea 1: Migraciones de Room y Estabilidad (Punto 2)
**Archivos:** `AppCasaDatabase.kt`, `DatabaseModule.kt`, `Migrations.kt` (nuevo).

- [x] **Paso 1:** Crear `core/data/src/main/java/com/appcasa/core/data/local/Migrations.kt` para definir objetos `Migration`.
- [x] **Paso 2:** Configurar `Room.databaseBuilder` en `DatabaseModule.kt` para añadir las migraciones y eliminar la política destructiva.
- [x] **Paso 3:** Incrementar la versión de la base de datos en `AppCasaDatabase.kt` y registrar las nuevas entidades si fuera necesario.

### Tarea 2: Proveedor de Contexto de Hogar (Punto 3)
**Archivos:** `CurrentHouseholdProvider.kt` (nuevo), ViewModels de todas las features.

- [x] **Paso 1:** Definir la interfaz `CurrentHouseholdProvider` en el módulo `core:domain`.
- [x] **Paso 2:** Implementar el proveedor en `core:data` usando DataStore o la tabla de Configuración.
- [x] **Paso 3:** Inyectar el proveedor en `TasksViewModel`, `FinanceViewModel`, `StockViewModel`, etc.
- [x] **Paso 4:** Sustituir todas las referencias a `1L` por una llamada reactiva al proveedor en el Dashboard.

### Tarea 3: Recordatorios Fiables y WorkManager (Punto 5)
**Archivos:** `WorkManagerReminderScheduler.kt`, `BootRescheduleReceiver.kt` (nuevo), `AndroidManifest.xml`.

- [x] **Paso 1:** Asegurar que `WorkManagerReminderScheduler` use etiquetas únicas por recordatorio.
- [x] **Paso 2:** Crear un `BroadcastReceiver` llamado `BootRescheduleReceiver` para reprogramar tareas pendientes tras un reinicio del dispositivo.
- [x] **Paso 3:** Registrar el receptor y el permiso `RECEIVE_BOOT_COMPLETED` en el `AndroidManifest.xml`.
- [x] **Paso 4:** Refinar `ReminderWorker` para manejar correctamente el tiempo de activación y evitar notificaciones "fantasma".

### Tarea 4: Refinamiento UX/UI y Validaciones (Punto 6)
**Archivos:** Componentes UI core, Pantallas de features.

- [x] **Paso 1:** Crear un componente `AppCasaEmptyState` en `core:ui` para mostrar cuando no hay tareas, gastos o mascotas.
- [x] **Paso 2:** Implementar validaciones visuales en los formularios (ej: evitar importes vacíos en Gastos o nombres vacíos en Tareas).
- [x] **Paso 3:** Unificar el diseño de las "Top Bars" y añadir feedback de carga (shimmer o progress) donde sea necesario.

---

## ✅ Lista de Verificación (Checklist) Final

- [x] La app compila y no se cierra al abrir por primera vez.
- [x] Los datos existentes persisten tras un cambio de versión de DB.
- [x] No existen strings `1L` hardcodeados en los ViewModels principales.
- [x] Los recordatorios se muestran en la hora exacta.
- [x] Todas las pantallas tienen un estado visual correcto cuando están vacías.
