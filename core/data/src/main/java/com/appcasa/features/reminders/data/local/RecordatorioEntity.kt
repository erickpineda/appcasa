package com.appcasa.features.reminders.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.TipoRepeticion
import com.appcasa.features.settings.data.local.HogarEntity

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
    Index("activo"),
    Index("sync_id")
  ]
)
data class RecordatorioEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

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
  val tareaId: Long? = null,

  // ID del miembro al que está vinculado — ej: vacuna de una mascota (opcional)
  @ColumnInfo(name = "miembro_id")
  val miembroId: Long? = null,

  // UUID del Worker de WorkManager — necesario para cancelarlo si se edita o elimina
  @ColumnInfo(name = "worker_id")
  val workerId: String? = null,

  @ColumnInfo(name = "notificado")
  val notificado: Boolean = false,

  @ColumnInfo(name = "activo")
  val activo: Boolean = true,

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)
