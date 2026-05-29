# Plan Maestro: Utilidades 100% Productivas y Foto a PDF

Este plan detalla la transformación del módulo de Utilidades para convertirlo en una herramienta de uso diario real, integrando nuevas funcionalidades y mejorando la persistencia y usabilidad de las actuales.

## User Review Required

> [!IMPORTANT]
> - **Permisos de Archivos**: La utilidad de PDF requerirá permisos para escribir en el almacenamiento público (Documents) para que los archivos sean accesibles fuera de la app.
> - **Lógica de Guardado**: ¿Deseas que los resultados de las calculadoras (como IMC) se guarden con fecha para ver una evolución, o solo el último valor?
> - **Nuevas Utilidades**: He propuesto un Generador de QR para WiFi y un Conversor de Cocina. ¿Hay alguna otra herramienta específica que uses mucho en casa?

---

## Proposed Changes

### 1. Refactorización de Utilidades Existentes
El objetivo es que dejen de ser calculadoras volátiles y pasen a ser herramientas con memoria y UI profesional.

#### BMICalculator, Mortgage, Savings, etc.
- **Persistencia**: Guardar los últimos valores introducidos en `ConfiguracionEntity` para que al volver a entrar no haya que escribir todo de nuevo.
- **UI Unificada**: Aplicar `AppCasaCard` con glassmorphism y mejorar los gráficos de resultados.

---

### 2. Nuevas Utilidades Útiles

#### [NEW] [CocinaConverterScreen](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/utilities/presentation/screen/CocinaConverterScreen.kt)
- Conversiones rápidas de cocina: tazas a gramos, cucharadas a ml, temperatura horno (F a C).

#### [NEW] [WifiQRScreen](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/utilities/presentation/screen/WifiQRScreen.kt)
- Generador de código QR para que las visitas se conecten al WiFi escaneando la pantalla.

---

### 3. Utilidad Estrella: Foto a PDF

#### [NEW] [PhotoToPdfScreen](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/utilities/presentation/screen/PhotoToPdfScreen.kt)
- **Funcionalidad**:
  1. Seleccionar múltiples fotos de la galería o cámara.
  2. Vista previa en miniatura con opción de reordenar o eliminar.
  3. Botón "Generar PDF".
  4. Guardado en la carpeta `Documents/AppCasa`.
  5. Opción inmediata de "Compartir PDF" vía WhatsApp/Email.
- **Tecnología**: Uso de `android.graphics.pdf.PdfDocument` nativo para ligereza.

#### [NEW] [PdfViewModel](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/utilities/presentation/viewmodel/PdfViewModel.kt)
- Lógica de procesamiento de imágenes y generación de archivos en hilo de fondo.

---

### 4. Actualización de Navegación y Datos

#### [Screen.kt](file:///C:/dev/android_wks/AppCasa/core/ui/src/main/java/com/appcasa/navigation/Screen.kt)
- Añadir rutas para `PhotoToPdf`, `WifiQR` y `CocinaConverter`.

#### [UtilitiesViewModel.kt](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/utilities/presentation/viewmodel/UtilitiesViewModel.kt)
- Registrar las nuevas utilidades en el sistema de inicialización.

---

## Verification Plan

### Manual Verification
1. **Flujo PDF**: Seleccionar 3 fotos, generar PDF, abrirlo con una app externa de lectura de PDFs y verificar que la calidad sea correcta.
2. **Persistencia**: Entrar en la calculadora de Hipoteca, poner datos, salir de la app, volver a entrar y verificar que los datos siguen ahí.
3. **Compartir**: Generar un QR de WiFi y probar a escanearlo con otro teléfono.
