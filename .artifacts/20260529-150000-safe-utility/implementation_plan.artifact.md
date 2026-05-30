# Plan: Gestor de Documentos y Garantías (Smart Safe)

Este proyecto busca crear la utilidad más productiva de la casa: un lugar seguro para digitalizar y controlar todo el papeleo importante, eliminando las pérdidas de tickets y las garantías olvidadas.

## 🎯 Objetivo
Transformar el caos de papeles físicos en una base de datos digital con alertas automáticas.

---

## 🛠️ Funcionalidades Clave

### 1. Digitalización Directa
*   Integración con la herramienta **Foto a PDF**: Sacas foto al ticket de compra y se convierte en un documento oficial del hogar.
*   Almacenamiento categorizado: *Facturas, Salud, Seguros, Garantías, Educación*.

### 2. Control de Garantías (Productividad Real)
*   Al guardar un ticket (ej: "Lavadora nueva"), introduces la fecha de fin de garantía.
*   **Automatización**: La app crea un evento en la **Agenda Familiar** avisándote 1 mes antes de que expire.
*   Evita perder dinero por no reclamar a tiempo.

### 3. El "Baúl" Familiar
*   Fichas por miembro: DNI digitalizado de Brian, carnés de vacunas de los perros, pólizas de seguro de Alicia.
*   Acceso instantáneo en emergencias (ej: tener el número de póliza a mano si se rompe una tubería).

---

## 🚀 Pasos de Implementación

1.  **Entidad `DocumentoEntity`**: Crear la tabla en la base de datos (Nombre, Categoría, URI del PDF, Fecha Expiración).
2.  **Pantalla de Gestión**: Una cuadrícula elegante con carpetas.
3.  **Lógica de Alerta**: Sincronización automática con el `ReminderScheduler` que ya tenemos.
4.  **Buscador**: Encontrar el ticket de "Aspiradora" en 2 segundos.

---

## ❓ Preguntas para Erick
1.  ¿Prefieres que los documentos se guarden solo en el móvil o te gustaría que en el futuro se puedan subir a la nube (Google Drive)?
2.  ¿Qué categorías de "papeles" son las que más guerra te dan en casa ahora mismo?
