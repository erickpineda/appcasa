# Walkthrough - Mejoras de Arquitectura y Modularización (Fases 1 y 2)

He completado las dos fases de refactorización planificadas para AppCasa, transformando el proyecto en una aplicación Android nativa moderna, escalable y con una lógica de negocio desacoplada.

## Cambios Realizados

### 1. Refactorización de Lógica de Negocio
Se ha eliminado la lógica de negocio que estaba mezclada en la capa de presentación (ViewModels), moviéndola a `UseCases` puros en el dominio:
- **Procesamiento OCR:** `ProcessTicketUseCase` ahora maneja la interpretación de tickets con ML Kit y Regex.
- **Gestión de Estados:** `UpdateMemberMoodUseCase` encapsula las reglas de actualización de emojis y timestamps.
- **Inventario:** `UpdateStockQuantityUseCase` asegura la integridad de los datos de stock y el auto-abastecimiento.

### 2. Modularización Avanzada
Se ha dividido el módulo contenedor `feature:dashboard` para mejorar la cohesión y reducir los tiempos de compilación:
- **Nuevo módulo `:feature:family`:** Centraliza la gestión de miembros y mascotas.
- **Nuevo módulo `:feature:lists`:** Gestiona las listas de compra y checklists de forma independiente.
- **Eliminación de dependencias cruzadas:** Se eliminó la dependencia directa de `:feature:settings` hacia `:feature:dashboard`, logrando un aislamiento real entre módulos.

### 3. Calidad y Estructura
- **Base de Testing:** Se ha configurado el entorno de pruebas unitarias con JUnit 4 y MockK. He implementado los primeros tests para los casos de uso críticos (`UpdateMemberMoodUseCaseTest`, `UpdateStockQuantityUseCaseTest`, `ProcessTicketUseCaseTest`).
- **Normalización de Paquetes:** Se reorganizaron todos los módulos para seguir un patrón de nombres consistente: `com.appcasa.features.[nombre_feature].presentation.[screen/viewmodel]`.
- **Organización de UseCases:** Los casos de uso en `:core:domain` se han agrupado por subcarpetas funcionales (`household`, `config`, `user`, `tasks`, etc.), mejorando la navegabilidad.

### 4. Sincronización de Documentación
- Se ha creado un nuevo `README.md` en castellano con el stack tecnológico real.
- Se ha actualizado el documento de **Arquitectura Técnica** eliminando referencias obsoletas a Ionic/Spring y reflejando la realidad nativa actual.

## Verificación Final

### Compilación y Construcción
- Se ejecutó `./gradlew :app:assembleDebug --rerun-tasks` con éxito.
- Todos los módulos utilizan el **SDK 35** de forma unificada.
- Las referencias de Hilt y la navegación se han verificado tras los cambios de paquetes.

### 4. Fase 3: Integración de Firebase y Sincronización
He completado la integración de Firebase para habilitar las funcionalidades colaborativas:

- **Infraestructura Cloud:**
    - Se activó el plugin `google-services` en el proyecto.
    - Se añadieron todas las dependencias necesarias de Firebase (Auth, Firestore, Messaging) en `:core:data`.
    - Se configuró Hilt para proveer las instancias de Firebase (`FirebaseModule.kt`).
- **Capa de Datos Remota:**
    - Se creó `FirestoreDataSource.kt` en `:core:data` para manejar la comunicación con la nube.
    - Se implementó un sistema de **DTOs** (`TaskDto.kt`) para desacoplar el modelo de dominio de la estructura de Firebase.
- **Sincronización Offline-First:**
    - Se configuró **WorkManager** con Hilt (`SyncWorker.kt`) para subir cambios locales a la nube en segundo plano.
    - Se implementó la sincronización en tiempo real (Remote -> Local) en `TasksRepositoryImpl` usando snapshots de Firestore.
    - El `DashboardViewModel` ahora inicia automáticamente la escucha de cambios remotos al cargar.
- **Notificaciones Colaborativas:**
    - Se implementó `AppFirebaseMessagingService.kt` para recibir avisos de la pareja.
    - Se añadió lógica de suscripción automática a "Topics" basados en el ID del hogar (`household_{id}`).

**Nota Importante:** Para que la aplicación compile y funcione en un dispositivo real, es necesario colocar el archivo `google-services.json` generado en la consola de Firebase dentro de la carpeta `/app`.

## Verificación Final

### Estado de la Arquitectura
El proyecto ahora cuenta con una arquitectura de primer nivel:
1.  **Modular:** Features aisladas y cohesivas.
2.  **Limpia:** Lógica de negocio en UseCases, presentación ligera.
3.  **Colaborativa:** Preparada para sincronización en la nube.
4.  **Documentada:** README y Guía Técnica actualizados.
5.  **Testeable:** Infraestructura de tests configurada y operativa.
