package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.appcasa.features.family.data.local.MiembroEntity

/**
 * Tabla pivote N:N entre Tarea y Miembro.
 * Una tarea puede asignarse a varios miembros del hogar.
 */
@Entity(
  tableName   = "tarea_asignaciones",
  primaryKeys = ["tarea_id", "miembro_id"],
  foreignKeys = [
    ForeignKey(
      entity        = TareaEntity::class,
      parentColumns = ["id"],
      childColumns  = ["tarea_id"],
      onDelete      = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity        = MiembroEntity::class,
      parentColumns = ["id"],
      childColumns  = ["miembro_id"],
      onDelete      = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index("tarea_id"),
    Index("miembro_id"),
    Index("sync_id")
  ]
)
data class TareaAsignacionEntity(

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "tarea_id")
  val tareaId: Long,

  @ColumnInfo(name = "tarea_sync_id")
  val tareaSyncId: String? = null,

  @ColumnInfo(name = "miembro_id")
  val miembroId: Long,

  @ColumnInfo(name = "miembro_sync_id")
  val miembroSyncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
