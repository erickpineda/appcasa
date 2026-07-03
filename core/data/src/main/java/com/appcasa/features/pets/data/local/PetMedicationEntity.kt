package com.appcasa.features.pets.data.local

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
  tableName = "mascota_medicaciones",
  foreignKeys = [
    ForeignKey(
      entity = MiembroEntity::class,
      parentColumns = ["id"],
      childColumns = ["mascota_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index("mascota_id")
  ]
)
data class PetMedicationEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "mascota_id")
  val mascotaId: String,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "dosis")
  val dosis: String,

  @ColumnInfo(name = "frecuencia")
  val frecuencia: String,

  @ColumnInfo(name = "fecha_inicio")
  val fechaInicio: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "fecha_fin")
  val fechaFin: Long? = null,

  @ColumnInfo(name = "activa")
  val activa: Boolean = true,

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
