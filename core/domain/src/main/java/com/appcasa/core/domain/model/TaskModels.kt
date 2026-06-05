package com.appcasa.core.domain.model

data class Task(
    val id: Long = 0,
    val hogarId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val categoriaId: Long? = null,
    val fechaLimite: Long? = null,
    val periodicidad: Periodicidad = Periodicidad.NINGUNA,
    val esPersonal: Boolean = false,
    val completadoEn: Long? = null,
    val fotoUri: String? = null,
    val anticipacionMins: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val puntosOtorgados: Boolean = false,
    val createdById: Long? = null,
    val archived: Boolean = false
)

data class Reward(
    val id: Long = 0,
    val hogarId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val costoPuntos: Int,
    val icono: String = "card_giftcard"
)

data class TaskCategory(
    val id: Long = 0,
    val hogarId: Long,
    val nombre: String,
    val colorHex: String = "2563EB",
    val icono: String? = null,
    val orden: Int = 0
)

data class TaskAssignment(
    val tareaId: Long,
    val miembroId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

data class TaskCheckItem(
    val id: Long = 0,
    val tareaId: Long,
    val texto: String,
    val completado: Boolean = false,
    val orden: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
