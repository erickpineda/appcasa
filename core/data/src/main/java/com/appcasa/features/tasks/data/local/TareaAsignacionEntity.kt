package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.features.family.data.local.MiembroEntity
import java.util.UUID

@Entity(
  tableName   = "tarea_asignaciones",
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
    Index(value = ["tarea_id", "miembro_id"], unique = true),
    Index("miembro_id")
  ]
)
data class TareaAsignacionEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "tarea_id")
  val tareaId: String,

  @ColumnInfo(name = "miembro_id")
  val miembroId: String,

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
