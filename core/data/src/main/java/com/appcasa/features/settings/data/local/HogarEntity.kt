package com.appcasa.features.settings.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.EstadoGeneral

@Entity(
  tableName = "hogares",
  indices = [
    Index("codigo_hogar"),
    Index("sync_id")
  ]
)
data class HogarEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  @ColumnInfo(name = "estado")
  val estado: String = EstadoGeneral.ACTIVO.name,

  // UUID para sincronización en la nube. Null hasta primera sync.
  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  // Código amigable para compartir con la pareja (ej: CASA-1234)
  @ColumnInfo(name = "codigo_hogar")
  val codigoHogar: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
