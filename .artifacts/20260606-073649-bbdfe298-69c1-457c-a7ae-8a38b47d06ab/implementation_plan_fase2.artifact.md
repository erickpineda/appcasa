# Plan de Implementación - Fase 2: Desacoplamiento y Calidad

Este plan aborda las deudas técnicas restantes identificadas en la revisión técnica, centrándose en el desacoplamiento de módulos, la normalización de la estructura y la base de testing.

## 1. Desacoplamiento de Módulos (Alta Prioridad)

### Eliminación de Dependencia `Settings -> Dashboard`
- **Problema:** `:feature:settings` depende de `:feature:dashboard`, lo que rompe la jerarquía modular.
- **Investigación:** Identificar qué componentes de `Dashboard` se usan en `Settings` (probablemente navegación o diálogos de selección).
- **Solución:**
    - Mover interfaces comunes a `:core:domain`.
    - Usar `Screen` objects de `:core:ui` o `:core:navigation` para la navegación sin dependencias directas entre features.
    - Eliminar `implementation(project(":feature:dashboard"))` de `feature/settings/build.gradle.kts`.

---

## 2. Infraestructura de Testing (Alta Prioridad)

### Configuración Base de Tests Unitarios
- **Objetivo:** Permitir la validación de la lógica de negocio extraída a los UseCases.
- **Acciones:**
    - Configurar JUnit 4 y MockK en `:core:domain`.
    - Crear un test base para UseCases.
    - Implementar los primeros tests para:
        - `ProcessTicketUseCase` (Validación de Regex OCR).
        - `UpdateStockQuantityUseCase` (Validación de límites y auto-compra).
        - `UpdateMemberMoodUseCase` (Validación de timestamps).

---

## 3. Normalización de Paquetes (Prioridad Media)

### Consistencia en `:feature:dashboard` y otros
- **Acciones:**
    - Mover pantallas en `feature:dashboard` de `com.appcasa.features.presentation.screen` a `com.appcasa.features.dashboard.presentation.screen`.
    - Homogeneizar los ViewModels a `com.appcasa.features.dashboard.presentation.viewmodel`.
    - Asegurar que todos los módulos sigan el patrón: `com.appcasa.features.[feature_name].[layer]`.

---

## 4. Reorganización de Casos de Uso (Prioridad Baja)

### Agrupación Funcional en `:core:domain`
- **Acciones:**
    - Crear subcarpetas en `core/domain/src/main/java/com/appcasa/core/domain/usecase/`:
        - `household/` (Gestión de hogar)
        - `user/` (Gestión de usuario/miembros)
        - `config/` (Configuraciones y flags)
    - Dividir archivos de UseCases masivos (como `FinanceUseCases.kt` o `FamilyUseCases.kt`) en archivos individuales si crecen demasiado.

---

## Plan de Verificación

### Pruebas Automatizadas
- Ejecutar `./gradlew test` para asegurar que los nuevos tests unitarios pasan.
- Verificar que no hay errores de compilación tras los cambios de paquetes.

### Pruebas Manuales
- Verificar que la navegación entre `Settings` y `Dashboard` sigue funcionando correctamente tras el desacoplamiento.
- Validar que el flujo de configuración de hogar (HouseSetup) no se ha visto afectado.

### Verificación de Estructura
- Ejecutar un análisis de dependencias de Gradle para confirmar que el grafo es acíclico y limpio.
