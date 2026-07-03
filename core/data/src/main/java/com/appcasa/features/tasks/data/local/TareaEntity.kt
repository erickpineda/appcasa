package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.TipoContenidoTarea
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.HogarEntity
import java.util.UUID

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
    ),
    ForeignKey(
      entity        = TareaEntity::class,
      parentColumns = ["id"],
      childColumns  = ["tarea_padre_id"],
      onDelete      = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index("hogar_id"),
    Index("categoria_id"),
    Index("tarea_padre_id"),
    Index("estado"),
    Index("fecha_limite")
  ]
)
data class TareaEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "hogar_id")
  val hogarId: String,

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
  val categoriaId: String? = null,

  // Referencia a la tarea original si esta fue generada por periodicidad
  @ColumnInfo(name = "tarea_padre_id")
  val tareaPadreId: String? = null,

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

  @ColumnInfo(name = "anticipacion_mins")
  val anticipacionMins: Int = 0,

  @ColumnInfo(name = "points")
  val points: Int = 10,

  @ColumnInfo(name = "puntos_otorgados")
  val puntosOtorgados: Boolean = false,

  @ColumnInfo(name = "archived")
  val archived: Boolean = false,

  // --- Auditoría / Sync ---
  @ColumnInfo(name = "created_at")
  override val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "created_by")
  override val createdBy: String? = null,

  @ColumnInfo(name = "updated_at")
  override val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_by")
  override val updatedBy: String? = null,

  @ColumnInfo(name = "deleted_at")
  override val deletedAt: Long? = null,

  @ColumnInfo(name = "deleted_by")
  override val deletedBy: String? = null,

  @ColumnInfo(name = "last_synced_at")
  override var lastSyncedAt: Long? = null

) : Syncable, Auditable
