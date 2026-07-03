package com.appcasa.core.domain.model

data class Task(
    val id: String = "",
    val hogarId: String = "",
    val titulo: String = "",
    val descripcion: String? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val categoriaId: String? = null,
    val fechaLimite: Long? = null,
    val periodicidad: Periodicidad = Periodicidad.NINGUNA,
    val esPersonal: Boolean = false,
    val completadoEn: Long? = null,
    val fotoUri: String? = null,
    val anticipacionMins: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val points: Int = 10,
    val puntosOtorgados: Boolean = false,
    val createdById: String? = null,
    val archived: Boolean = false,
    val lastSyncedAt: Long? = null
)

data class Reward(
    val id: String = "",
    val hogarId: String = "",
    val titulo: String = "",
    val descripcion: String? = null,
    val costePuntos: Int = 0,
    val icono: String = "card_giftcard",
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class TaskCategory(
    val id: String = "",
    val hogarId: String = "",
    val nombre: String = "",
    val colorHex: String = "2563EB",
    val icono: String? = null,
    val orden: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class TaskAssignment(
    val tareaId: String = "",
    val miembroId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

data class TaskCheckItem(
    val id: String = "",
    val tareaId: String = "",
    val texto: String = "",
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
