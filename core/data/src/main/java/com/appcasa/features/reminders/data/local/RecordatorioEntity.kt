package com.appcasa.features.reminders.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.core.domain.model.TipoRepeticion
import com.appcasa.features.settings.data.local.HogarEntity
import java.util.UUID

@Entity(
  tableName = "recordatorios",
  foreignKeys = [ForeignKey(
    entity        = HogarEntity::class,
    parentColumns = ["id"],
    childColumns  = ["hogar_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("hogar_id"),
    Index("fecha_hora"),
    Index("activo")
  ]
)
data class RecordatorioEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "hogar_id")
  val hogarId: String,

  @ColumnInfo(name = "titulo")
  val titulo: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  // Fecha y hora del recordatorio en epoch millis
  @ColumnInfo(name = "fecha_hora")
  val fechaHora: Long,

  // TipoRepeticion: NINGUNA, DIARIA, SEMANAL, MENSUAL, ANUAL
  @ColumnInfo(name = "tipo_repeticion")
  val tipoRepeticion: String = TipoRepeticion.NINGUNA.name,

  // ID de la tarea asociada (opcional)
  @ColumnInfo(name = "tarea_id")
  val tareaId: String? = null,

  // ID del miembro al que está vinculado (opcional)
  @ColumnInfo(name = "miembro_id")
  val miembroId: String? = null,

  // UUID del Worker de WorkManager
  @ColumnInfo(name = "worker_id")
  val workerId: String? = null,

  @ColumnInfo(name = "notificado")
  val notificado: Boolean = false,

  @ColumnInfo(name = "activo")
  val activo: Boolean = true,

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
