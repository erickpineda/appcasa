package com.appcasa.core.domain.model

// ─── MIEMBRO ──────────────────────────────────────────
enum class TipoMiembro {
  PERSONA, PERRO, GATO, TORTUGA, AVE, CONEJO, OTRO
}

enum class EstadoGeneral {
  ACTIVO, INACTIVO, ELIMINADO
}

enum class RolHogar {
  ADMIN, COLABORADOR, SOLO_LECTURA
}

// ─── TAREA ────────────────────────────────────────────
enum class Prioridad {
  ALTA, MEDIA, BAJA
}

enum class EstadoTarea {
  PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA
}

enum class TipoContenidoTarea {
  TEXTO, LISTA
}

enum class Periodicidad {
  NINGUNA, DIARIA, SEMANAL, QUINCENAL,
  MENSUAL, TRIMESTRAL, ANUAL, PERSONALIZADA
}

// ─── RECORDATORIO ─────────────────────────────────────
enum class TipoRepeticion {
  NINGUNA, DIARIA, SEMANAL, MENSUAL, ANUAL
}

// ─── EVENTO ───────────────────────────────────────────
enum class TipoEvento {
  CUMPLEANOS, CITA_VETERINARIO, VACUNA,
  DESPARASITACION, ITV, SEGURO, REUNION, OTRO
}

// ─── LISTA ────────────────────────────────────────────
enum class TipoLista {
  COMPRA, FARMACIA, VETERINARIO,
  VIAJE, ESCOLAR, PERSONALIZADA
}

// ─── MASCOTA ──────────────────────────────────────────
enum class TipoDesparasitacion {
  INTERNA, EXTERNA, AMBAS
}

// ─── CONFIGURACIÓN ────────────────────────────────────
enum class TipoConfiguracion {
  STRING, INT, BOOLEAN, JSON
}
