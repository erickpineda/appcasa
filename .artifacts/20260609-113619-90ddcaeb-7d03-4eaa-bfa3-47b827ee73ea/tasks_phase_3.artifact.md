# Plan de Tareas - Fase 3: Funcionalidades Avanzadas y Pulido

Este documento detalla las tareas específicas para completar las funcionalidades restantes del plan maestro.

## 1. Identificación Física mediante QR (Mantenimiento)
Permitir que los objetos del hogar tengan un "ID físico" escaneable.

- [x] **Task 1.1**: Añadir botón "Generar QR de Identificación" en la pantalla `MaintenanceDetailScreen`.
- [x] **Task 1.2**: Crear un diálogo para previsualizar el QR generado (usando `QRUtils`).
- [x] **Task 1.3**: Implementar el escáner de QR en la pantalla principal de Mantenimiento que reconozca el formato `appcasa://maintenance/{id}`.
- [x] **Task 1.4**: Configurar la navegación automática al detalle del objeto tras el escaneo exitoso.

## 2. Sincronización de Multimedia (Fotos de Perfil y Mascotas)
Asegurar que las imágenes viajen entre dispositivos.

- [x] **Task 2.1**: Actualizar `MemberDto` y `FamilyMember` para manejar `urlNube` (URL de descarga de Firebase Storage).
- [x] **Task 2.2**: Implementar lógica de subida de archivos en `FamilyRemoteDataSource` usando `FirebaseStorage`.
- [x] **Task 2.3**: Integrar en el `SyncWorker` la subida de fotos locales de miembros y mascotas.
- [x] **Task 2.4**: Actualizar la carga de imágenes en la UI para priorizar la URL de la nube si la foto local no existe (escenario de nuevo teléfono).

## 3. Feedback Visual de Sincronización
Informar al usuario sobre el estado de sus datos.

- [x] **Task 3.1**: Crear un componente de Compose `SyncStatusBadge` (un pequeño icono de nube).
- [x] **Task 3.2**: Integrar el badge en las tarjetas de `Task`, `Expense` y `PostIt`.
- [x] **Task 3.3**: Implementar lógica: Mostrar nube con flecha si `updated_at > last_synced_at`, y ocultarla (o mostrar check verde) si ya está en la nube.

## 4. Economía del Hogar: Canje de Recompensas
Cerrar el ciclo de gamificación.

- [x] **Task 4.1**: Implementar función `redeemReward(reward)` en `RewardStoreViewModel`.
- [x] **Task 4.2**: Añadir validación: No permitir el canje si el usuario no tiene puntos suficientes.
- [x] **Task 4.3**: Implementar la resta de puntos en `FamilyRepository` y sincronizar el nuevo saldo con Firebase.
- [x] **Task 4.4**: Mostrar una animación de celebración sencilla al canjear con éxito.

## 5. Notificaciones Remotas (Firebase Cloud Messaging)
Comunicación real entre diferentes teléfonos de la familia.

- [x] **Task 5.1**: Crear la clase `AppFirebaseMessagingService` para recibir mensajes push.
- [x] **Task 5.2**: Implementar suscripción automática a "temas" (topics) basados en el ID del hogar (`household_{id}`).
- [x] **Task 5.3**: Configurar disparadores básicos para que al cambiar un Post-it o Tarea, se envíe un aviso al canal del hogar (Requiere lógica en Firebase o simulación mediante triggers).

---

## Orden de Ejecución Sugerido
1. **Canje de Recompensas** (Rápido y alto impacto en UX).
2. **Sincronización de Fotos** (Crítico para la integridad de backups).
3. **QR de Mantenimiento** (Funcionalidad innovadora).
4. **Feedback Visual y Notificaciones** (Pulido final).
