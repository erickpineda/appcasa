# Informe de Refuerzo de Seguridad - AppCasa

Se han implementado múltiples medidas para mitigar las vulnerabilidades identificadas en el análisis de seguridad. A continuación se detallan los cambios aplicados:

## 1. Cifrado de Base de Datos en Reposo (SQLCipher)
**Vulnerabilidad:** La base de datos SQLite de Room almacenaba información sensible en texto plano.
**Solución:** Se ha integrado **SQLCipher** para cifrar todo el archivo de la base de datos mediante AES-256.
- **Implementación:**
    - Añadida la dependencia `net.zetetic:android-database-sqlcipher` en `libs.versions.toml` y `app/build.gradle.kts`.
    - En `SecurityModule.kt`, se genera y almacena una frase de paso (passphrase) única y aleatoria dentro de `EncryptedSharedPreferences`.
    - En `DatabaseModule.kt`, se ha configurado el `SupportFactory` de SQLCipher para utilizar dicha frase de paso.

## 2. Protección de Copias de Seguridad (Backups)
**Vulnerabilidad:** Los datos de la aplicación podían ser extraídos mediante copias de seguridad de ADB o la nube.
**Solución:** Se ha desactivado la capacidad de realizar copias de seguridad automáticas para evitar la fuga de datos.
- **Cambio:** En `AndroidManifest.xml`, se ha modificado el atributo `android:allowBackup` a `false`.

## 3. Cierre de Componentes Expuestos
**Vulnerabilidad:** `GeofenceBroadcastReceiver` estaba marcado como `exported="true"`, permitiendo que aplicaciones maliciosas inyectaran notificaciones falsas.
**Solución:** Se ha restringido el acceso al componente únicamente a la propia aplicación y al sistema operativo para la gestión de geovallas.
- **Cambio:** En `AndroidManifest.xml`, se ha modificado `android:exported="false"` para el receptor `GeofenceBroadcastReceiver`.

## 4. Gestión de Secretos en Android Keystore
**Mejora:** La clave de cifrado de la base de datos no está hardcodeada; se gestiona de forma dinámica y se guarda en un contenedor seguro (`EncryptedSharedPreferences`), que a su vez utiliza el hardware de seguridad del dispositivo (Keystore) para proteger la clave maestra.

---
**Estado Final:** La superficie de ataque de AppCasa se ha reducido significativamente, protegiendo tanto los datos almacenados localmente como los puntos de entrada de comunicación entre aplicaciones.