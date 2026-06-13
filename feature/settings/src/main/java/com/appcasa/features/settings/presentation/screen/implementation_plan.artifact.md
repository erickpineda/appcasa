# Reorganización de Ajustes y Optimización del Dashboard

Este plan detalla la reestructuración de la pantalla de ajustes para una mejor jerarquía visual y la priorización de los Post-its en el Dashboard por defecto.

## User Review Required

- **Orden de secciones**: He propuesto un orden que prioriza Cuenta y Seguridad, seguido de Personalización y finalmente Gestión del Hogar. ¿Es este el orden que prefieres?
- **Gestión de cuenta Google**: Cuando la cuenta está vinculada a Google, desactivaremos la opción de cambiar contraseña/email localmente y mostraremos un indicador de "Gestionado por Google".

## Proposed Changes

### Dashboard

Priorizar los Post-its en el orden predeterminado de los módulos.

#### [DashboardViewModel.kt](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/dashboard/presentation/viewmodel/DashboardViewModel.kt)

- Cambiar el orden de `defaultModules` para que `Constants.Modules.POSTITS` sea el primero.

---

### Settings

Reorganizar las secciones y mejorar la experiencia de "Mi Cuenta".

#### [SettingsScreen.kt](file:///C:/dev/android_wks/AppCasa/feature/settings/src/main/java/com/appcasa/features/settings/presentation/screen/SettingsScreen.kt)

- **Reordenar `SettingsHub`**:
    1. Perfil (Avatar + Nombre)
    2. Vincular Cuenta (Condicional)
    3. **Cuenta y Seguridad** (Anteriormente ACCOUNT)
    4. **Personalización** (Anteriormente APPEARANCE)
    5. **Preferencias**
    6. **Gestión del Hogar** (Anteriormente HOUSEHOLD)
    7. **Sistema**
    8. **Cerrar Sesión**
- **Mejorar `MiCuentaSection`**:
    - Detectar si la cuenta es de Google (mediante `providerData` de Firebase o `authId`).
    - Si es Google:
        - Mostrar un banner o item indicando "Cuenta gestionada por Google".
        - Deshabilitar o esconder "Cambiar Email" y "Cambiar Contraseña".
        - Mantener "Bloqueo con Biometría".
- **Actualizar `SettingsSection` enum**: Reordenar para que coincida con el flujo visual si es necesario (aunque el orden real lo da el `LazyColumn`).

---

## Verification Plan

### Automated Tests
- No hay tests unitarios específicos para el orden de la UI, pero verificaré que los UseCases se sigan llamando correctamente.

### Manual Verification
- **Dashboard**: Abrir la app con una base de datos limpia (o resetear ajustes) y verificar que los Post-its aparecen arriba del todo.
- **Settings**:
    - Verificar el nuevo orden de las secciones.
    - Vincular con Google y entrar en "Mi Cuenta" para ver que las opciones de email/password están bloqueadas/escondidas y aparece el indicador de Google.
    - Cerrar sesión y verificar que el botón sigue al final.
