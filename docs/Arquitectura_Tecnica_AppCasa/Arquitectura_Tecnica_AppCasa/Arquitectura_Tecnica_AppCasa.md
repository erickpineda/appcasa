**AppCasa**

Documento de Arquitectura Técnica

Versión 1.1 · Post-Refactor

Junio 2026

# **1. Visión y objetivos**
AppCasa es una aplicación Android nativa diseñada como un sistema operativo del hogar. Su arquitectura es modular y descentralizada: cada funcionalidad principal reside en su propio módulo de feature, lo que permite un desarrollo independiente y una alta cohesión.

## **1.1 Objetivos técnicos**
- Arquitectura modular basada en features.
- Clean Architecture (Domain, Data, Presentation).
- Interfaz moderna con Jetpack Compose.
- Inyección de dependencias con Hilt.
- Persistencia local robusta con Room.
- Procesamiento de imágenes (OCR) y Barcodes con ML Kit.
- Gestión de tareas en segundo plano con WorkManager.

# **2. Stack tecnológico**

|**Capa**|**Tecnología**|
| :- | :- |
|Lenguaje|Kotlin 2.x|
|UI Framework|Jetpack Compose|
|Dependency Injection|Hilt|
|Base de datos local|Room|
|Navegación|Navigation Compose|
|Background Tasks|WorkManager|
|Imágenes|Coil|
|OCR / Barcodes|Google ML Kit|
|Testing|JUnit 4, MockK, Espresso (planeado)|

# **3. Arquitectura Modular**
El proyecto se divide en módulos para facilitar la escalabilidad:

### **Módulos Core**
- **:core:ui**: Componentes visuales reutilizables, temas y estilos globales.
- **:core:domain**: Modelos de datos puros, interfaces de repositorio y lógica de negocio transversal.
- **:core:data**: Implementaciones de repositorios, base de datos Room y fuentes de datos externas.

### **Módulos de Feature**
- **:feature:dashboard**: Centro de control principal, búsqueda global y widgets.
- **:feature:family**: Gestión de miembros del hogar (humanos y mascotas) y perfiles.
- **:feature:lists**: Listas de compra y checklists personalizadas.
- **:feature:tasks**: Sistema de gestión de tareas con prioridades y XP.
- **:feature:finance**: Seguimiento de gastos y escaneo OCR de tickets.
- **:feature:inventory**: Control de stock con escaneo de códigos de barras.
- **:feature:calendar**: Calendario de eventos y recordatorios locales.
- **:feature:settings**: Configuración del hogar y preferencias de usuario.

# **4. Diseño de Capas (Clean Architecture)**
Cada feature sigue internamente la separación en capas:

1.  **Presentation**: ViewModels y pantallas de Compose. Los ViewModels delegan la lógica a los UseCases.
2.  **Domain**: Casos de uso (UseCases) que encapsulan las reglas de negocio.
3.  **Data**: (Delegado a `:core:data` o implementado localmente si es específico) Repositorios y DAOs.

# **5. Persistencia y Datos**
Room se utiliza para almacenar toda la información localmente, permitiendo el funcionamiento offline.
- **Hilt** provee las instancias de los Repositorios a los UseCases.
- **WorkManager** se encarga de tareas periódicas como limpieza de archivos antiguos o recordatorios de mantenimiento.

# **6. Navegación**
Centralizada en el módulo `:app` mediante `AppNavigation.kt`, utilizando rutas basadas en strings y argumentos definidos en `Screen.kt`.

# **7. Conclusión**
AppCasa ha evolucionado de un concepto híbrido a una aplicación Android nativa de alto rendimiento. Su estructura modular garantiza que el crecimiento futuro (más de 10-15 módulos) sea manejable y mantenible.
