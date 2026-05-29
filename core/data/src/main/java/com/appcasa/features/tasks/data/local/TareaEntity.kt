package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
  tableName = "tareas",
  foreignKeys = [
    ForeignKey(
      entity        = HogarEntity::class,
      parentColumns = ["id"],
      childColumns  = ["hogar_id"],
      onDelete      = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity        = CategoriaTareaEntity::class,
      parentColumns = ["id"],
      childColumns  = ["categoria_id"],
      onDelete      = ForeignKey.SET_NULL
    )
  ],
  indices = [
    Index("hogar_id"),
    Index("categoria_id"),
    Index("estado"),
    Index("fecha_limite")
  ]
)
data class TareaEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  @ColumnInfo(name = "titulo")
  val titulo: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  // Prioridad: ALTA, MEDIA, BAJA
  @ColumnInfo(name = "prioridad")
  val prioridad: String = Prioridad.MEDIA.name,

  // Tipo Contenido: TEXTO, LISTA
  @ColumnInfo(name = "tipo_contenido")
  val tipoContenido: String = TipoContenidoTarea.LISTA.name,

  // EstadoTarea: PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA
  @ColumnInfo(name = "estado")
  val estado: String = EstadoTarea.PENDIENTE.name,

  @ColumnInfo(name = "categoria_id")
  val categoriaId: Long? = null,

  @ColumnInfo(name = "fecha_limite")
  val fechaLimite: Long? = null,

  // Periodicidad: NINGUNA, DIARIA, SEMANAL...
  @ColumnInfo(name = "periodicidad")
  val periodicidad: String = Periodicidad.NINGUNA.name,

  // Si es true, solo la ve el usuario que la creó
  @ColumnInfo(name = "es_personal")
  val esPersonal: Boolean = false,

  // Epoch millis del momento en que se marcó como completada
  @ColumnInfo(name = "completado_en")
  val completadoEn: Long? = null,

  // URI de la foto adjunta (ej: estado de una avería)
  @ColumnInfo(name = "foto_uri")
  val fotoUri: String? = null,

  @ColumnInfo(name = "sync_id", index = true)
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)
