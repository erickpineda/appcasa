package com.appcasa.features.settings.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.core.domain.model.EstadoGeneral
import java.util.UUID

@Entity(
  tableName = "hogares",
  indices = [
    Index("codigo_hogar")
  ]
)
data class HogarEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  @ColumnInfo(name = "estado")
  val estado: String = EstadoGeneral.ACTIVO.name,

  // Código amigable para compartir con la pareja (ej: CASA-1234)
  @ColumnInfo(name = "codigo_hogar")
  val codigoHogar: String? = null,

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
