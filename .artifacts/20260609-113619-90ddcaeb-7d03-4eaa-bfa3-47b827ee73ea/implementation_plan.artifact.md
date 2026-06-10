# Plan de Desarrollo y Mejora - AppCasa

Este documento detalla la hoja de ruta para completar la arquitectura de AppCasa, asegurando que todas las funcionalidades locales se sincronicen correctamente con la nube de Firebase y se mejore la experiencia de usuario.

## 1. Sincronización Avanzada (Cloud Sync)
El objetivo es asegurar que ningún dato se quede "solo en el móvil".

- **Sub-elementos de Tareas**: Sincronizar listas de control (checklists) y asignaciones de miembros a tareas específicas.
- **Salud de Mascotas**: Extender la sincronización para incluir el historial de vacunas, desparasitaciones, registros de peso y medicaciones activas.
- **Gestión de Archivos (Smart Safe)**: Implementar la subida física de archivos PDF a Firebase Storage para que sean accesibles desde cualquier dispositivo vinculado al hogar.

## 2. Gamificación y Lógica de Puntos
Hacer que la gestión del hogar sea divertida y gratificante.

- **Sistema de Recompensas (XP)**: Implementar la lógica para que completar tareas otorgue puntos de experiencia.
- **Tienda Familiar**: Permitir el canje de puntos por recompensas configuradas por el administrador del hogar.
- **Niveles de Usuario**: Visualización de progreso y niveles basados en la colaboración histórica.

## 3. Notificaciones y Comunicación (FCM)
Mantener a la familia informada en tiempo real.

- **Notificaciones de Actividad**: Avisos cuando un miembro añade un Post-it, completa una tarea crítica o actualiza un gasto.
- **Alertas de Mantenimiento**: Recordatorios push sobre revisiones de caldera, filtros o citas veterinarias próximas.
- **Mensajería Sutil**: Mejorar los mensajes de estado (Toasts/Snackbars) para informar sobre el estado de la sincronización.

## 4. Utilidades Avanzadas e Interacción Física
Añadir valor práctico al día a día.

- **QR de Mantenimiento**: Generación de códigos QR únicos para objetos físicos. Al escanearlos, la app abre directamente el historial de mantenimiento de ese objeto.
- **Mejora de Avatares**: Sistema de carga y recorte de fotos de perfil para miembros y mascotas, sincronizado con Storage.
- **Modo Offline Resiliente**: Indicadores visuales claros de "Pendiente de sincronizar" para cuando no hay conexión.

## 5. Seguridad y Portabilidad de Datos (Backup & Recovery)
Asegurar la integridad de la información y la facilidad de migración entre dispositivos.

- **Sincronización Manual (Force Sync)**: Botón en Ajustes para forzar una subida/bajada inmediata de todos los datos a la nube, útil tras una reinstalación.
- **Reconstrucción Automática**: Lógica para que al iniciar sesión en un dispositivo nuevo, la app descargue automáticamente todo el histórico del hogar desde Firestore.
- **Auditoría de Integridad**: Mecanismo para detectar datos corruptos o conflictos de versión entre el móvil y la nube, resolviendo a favor de la versión más reciente (`updated_at`).
- **Exportación de Seguridad**: Opción para exportar un resumen de los datos del hogar (PDF/JSON) como copia de seguridad externa "fuera de la nube".

---

## Plan de Acción Inmediato

### Paso 1: Sincronización Total de Mascotas
Actualizaremos el `SyncWorker` y los `RemoteDataSources` para manejar las tablas de vacunas, pesos y medicinas.

### Paso 2: Lógica de Gamificación
Conectaremos el `TasksRepository` con el `FamilyRepository` para que la XP fluya al completar deberes.

### Paso 3: Subida de Archivos al Baúl (Storage)
Configuraremos el cliente de Firebase Storage para manejar los documentos del Smart Safe.

---

## Verificación
- **Pruebas de Integración**: Verificar en Firebase Console que cada nueva tabla/colección se crea correctamente.
- **Pruebas de Multi-dispositivo**: Asegurar que al añadir un dato en un terminal, aparece en el otro tras la sincronización.
- **Logs de Sync**: Monitorizar `WorkManager` para asegurar que las tareas de fondo no fallan.
