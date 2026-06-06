# AppCasa

**AppCasa** es una aplicación Android moderna diseñada como un "Sistema Operativo para el Hogar". Ayuda a las familias a gestionar tareas, finanzas, inventario, miembros de la familia (incluyendo mascotas) y mucho más, todo de una manera modular y escalable.

## Stack Tecnológico
- **Lenguaje:** Kotlin
- **Framework de UI:** Jetpack Compose
- **Arquitectura:** Clean Architecture + MVVM + Modularización
- **Inyección de Dependencias:** Hilt
- **Base de Datos Local:** Room
- **Tareas en Segundo Plano:** WorkManager
- **Navegación:** Navigation Compose
- **APIs Externas:** Google ML Kit (OCR y Escaneo de Barcodes), Firebase (Auth, Firestore, FCM)

## Estructura del Proyecto
El proyecto está organizado en múltiples módulos para asegurar una alta cohesión y bajo acoplamiento:

### Módulos Core
- `:core:ui`: Componentes de UI comunes, temas y utilidades visuales.
- `:core:domain`: Lógica de negocio pura, modelos de entidad e interfaces de repositorios.
- `:core:data`: Implementaciones de fuentes de datos (Room DAOs, implementaciones de repositorios, providers).

### Módulos de Feature (Funcionalidades)
- `:feature:dashboard`: Centro de control principal y resumen general.
- `:feature:family`: Gestión de miembros de la familia y perfiles del hogar (incluyendo mascotas).
- `:feature:lists`: Listas de la compra y checklists personalizadas.
- `:feature:tasks`: Gestión de tareas, prioridades y sistema de recompensas (XP).
- `:feature:finance`: Seguimiento de gastos y procesamiento OCR de tickets.
- `:feature:inventory`: Gestión de stock del hogar y escaneo de códigos de barras.
- `:feature:calendar`: Gestión de eventos y recordatorios locales.
- `:feature:settings`: Configuración del hogar y de la aplicación.

## Principios de Arquitectura
1. **Clean Architecture:** Separación clara de responsabilidades entre las capas de Presentación, Dominio y Datos.
2. **Orientado a Casos de Uso:** La lógica de negocio está encapsulada en UseCases pequeños y testeables.
3. **Modularización por Feature:** Cada funcionalidad principal reside en su propio módulo, facilitando el desarrollo en paralelo y compilaciones más rápidas.

## Primeros Pasos
1. Clona el repositorio.
2. Abre el proyecto en Android Studio (Koala o superior).
3. Compila el proyecto usando `./gradlew assembleDebug`.
4. Ejecútalo en un dispositivo o emulador con API 26+.
