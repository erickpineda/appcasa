package com.appcasa.features.pets.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.TipoDesparasitacion
import com.appcasa.features.family.data.local.MiembroEntity

@Entity(
  tableName = "mascota_desparasitaciones",
  foreignKeys = [ForeignKey(
    entity        = MiembroEntity::class,
    parentColumns = ["id"],
    childColumns  = ["mascota_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("mascota_id"),
    Index("fecha_proxima")
  ]
)
data class MascotaDesparasitacionEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "mascota_id")
  val mascotaId: Long,

  // TipoDesparasitacion: INTERNA, EXTERNA, AMBAS
  @ColumnInfo(name = "tipo")
  val tipo: String = TipoDesparasitacion.AMBAS.name,

  // Nombre del producto — ej: "Advocate", "Frontline", "Milbemax"
  @ColumnInfo(name = "producto")
  val producto: String? = null,

  @ColumnInfo(name = "fecha_aplicacion")
  val fechaAplicacion: Long,

  @ColumnInfo(name = "fecha_proxima")
  val fechaProxima: Long? = null,

  @ColumnInfo(name = "notas")
  val notas: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
