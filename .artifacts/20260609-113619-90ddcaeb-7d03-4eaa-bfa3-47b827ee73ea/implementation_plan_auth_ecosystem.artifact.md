# Plan de Ecosistema de Usuarios y Seguridad - AppCasa

Este documento detalla la hoja de ruta para implementar un sistema robusto de Autenticación y Seguridad, vinculando las cuentas de Firebase con la estructura de Hogares y Miembros.

## 1. Autenticación Obligatoria (Auth-First)
Asegurar que cada acción en la nube esté respaldada por una identidad real.

- [x] **Task 1.1**: Modificar `HouseSetupScreen` para que los botones "Crear Hogar" y "Unirme a un Hogar" redirijan a la pantalla de **Login/Registro** si no hay una sesión activa de Firebase.
- [x] **Task 1.2**: Mejorar `AuthScreen` con validaciones de campos (email válido, contraseña fuerte) y manejo de estados de error específicos de Firebase (usuario ya existe, contraseña incorrecta).
- [x] **Task 1.3**: Implementar "Recordar sesión" para que el usuario no tenga que loguearse cada vez que abre la app.

## 2. Resiliencia Offline (Hybrid Auth)
Garantizar que la app sea utilizable sin internet una vez configurada.

- [x] **Task 2.0**: Modificar el flujo de arranque para que **siempre** priorice los datos locales de Room. Si ya hay un hogar configurado localmente, entrar al Dashboard directamente sin esperar a Firebase Auth.
- [x] **Task 2.1**: Implementar un estado de "Sesión en Caché": Usar el usuario guardado localmente en Room para la lógica de UI mientras Firebase Auth intenta reconectar en segundo plano.
- [x] **Task 2.2**: Mostrar un indicador visual sutil (ej. icono de nube con advertencia) si la sesión ha expirado y no se puede renovar por falta de red, indicando que los cambios no se subirán hasta reconectar.

## 3. Vinculación Identidad-Dato (UID Linkage)
Conectar al usuario de Firebase con su representación en el hogar.

- [x] **Task 3.1**: Añadir el campo `firebaseUid` a `FamilyMember`, `MiembroEntity` y `MemberDto`.
- [x] **Task 3.2**: Actualizar `CreateHouseholdUseCase`: Al crear el hogar, el primer miembro (ADMIN) debe guardar el `uid` del usuario autenticado actual.
- [x] **Task 3.3**: Actualizar `JoinHouseholdUseCase`: Al unirse con código, vincular el `uid` del usuario al nuevo miembro creado o al existente si coincide el email.
- [x] **Task 3.4**: Refactorizar `UserRepositoryImpl` para que `getCurrentUser()` obtenga los datos directamente de la sesión activa de Firebase, sincronizándolos con la caché local de Room.

## 4. Seguridad en la Nube (Firebase Rules)
Proteger los datos para que solo los miembros de un hogar vean su propia información.

- [x] **Task 4.1**: Diseñar e implementar las **Firestore Security Rules** (Preparado en código con UIDs).
- [x] **Task 4.2**: Implementar reglas de **Firebase Storage** para que las fotos de un hogar solo sean accesibles por sus miembros.

## 5. Gestión de Cuenta y Recuperación
Facilitar la movilidad entre dispositivos y la gestión de credenciales.

- [x] **Task 5.1**: Añadir sección "Mi Cuenta" en Ajustes para cambiar contraseña o actualizar el email.
- [x] **Task 5.2**: Implementar el flujo de "Contraseña Olvidada" (Email de recuperación).
- [x] **Task 5.3**: Automatizar la **Detección de Hogares**: Al loguearse en un dispositivo nuevo, la app debe consultar Firestore en busca de todos los hogares donde el `uid` del usuario aparezca en la colección `members`.

## 6. Robustez en Actualizaciones
Prevenir la pérdida de datos en futuras versiones de la app.

- [x] **Task 6.1**: Implementar un sistema de "Pre-flight check": Al abrir la app, verificar que la versión local de la DB es compatible con la estructura de la nube.
- [x] **Task 6.2**: Mecanismo de **Logout Limpio**: Al cerrar sesión, limpiar la base de datos local de Room para evitar que el siguiente usuario vea datos del anterior (Privacy first).

## 7. Seguridad Avanzada y Privacidad
Añadir capas de protección para los datos sensibles del hogar.

- [x] **Task 7.1**: Implementar **Bloqueo Biométrico** (Huella/Cara) opcional para abrir la aplicación o entrar a módulos sensibles (Smart Safe).
- [x] **Task 7.2**: Configurar **Protección de Pantalla** (Secure Flag): Impedir capturas de pantalla en secciones privadas (como el baúl de documentos) y ofuscar la miniatura de la app en el menú de "Apps Recientes".
- [x] **Task 7.3**: Uso de **EncryptedSharedPreferences** para guardar cualquier clave de sesión o configuración sensible que deba persistir fuera de Room.
- [x] **Task 7.4**: Implementar validaciones de **Password Strength** en el registro para evitar cuentas vulnerables.

---

## Estado Final: **COMPLETADO**
La arquitectura de seguridad y usuario ha sido totalmente integrada en el núcleo de AppCasa.
