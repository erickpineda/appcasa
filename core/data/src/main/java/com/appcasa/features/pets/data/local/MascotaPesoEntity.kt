package com.appcasa.features.pets.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.family.data.local.MiembroEntity

@Entity(
  tableName = "mascota_pesos",
  foreignKeys = [ForeignKey(
    entity        = MiembroEntity::class,
    parentColumns = ["id"],
    childColumns  = ["mascota_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("mascota_id"),
    Index("fecha")
  ]
)
data class MascotaPesoEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "mascota_id")
  val mascotaId: Long,

  @ColumnInfo(name = "peso_kg")
  val pesoKg: Double,

  @ColumnInfo(name = "fecha")
  val fecha: Long,

  @ColumnInfo(name = "notas")
  val notas: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
