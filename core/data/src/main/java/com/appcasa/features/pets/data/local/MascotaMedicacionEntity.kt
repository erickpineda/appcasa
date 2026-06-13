package com.appcasa.features.pets.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.family.data.local.MiembroEntity

@Entity(
  tableName = "mascota_medicaciones",
  foreignKeys = [ForeignKey(
    entity        = MiembroEntity::class,
    parentColumns = ["id"],
    childColumns  = ["mascota_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("mascota_id"),
    Index("activa"),
    Index("sync_id")
  ]
)
data class MascotaMedicacionEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "mascota_id")
  val mascotaId: Long,

  @ColumnInfo(name = "mascota_sync_id")
  val mascotaSyncId: String? = null,

  // Nombre del medicamento — ej: "Apoquel", "Atopica"
  @ColumnInfo(name = "nombre")
  val nombre: String,

  // Dosis en texto libre — ej: "5 ml", "1 comprimido", "1/2 pastilla"
  @ColumnInfo(name = "dosis")
  val dosis: String,

  // Frecuencia en texto libre — ej: "Cada 12 horas", "Una vez al día", "Semanal"
  @ColumnInfo(name = "frecuencia")
  val frecuencia: String,

  @ColumnInfo(name = "fecha_inicio")
  val fechaInicio: Long,

  // Null si el tratamiento es indefinido
  @ColumnInfo(name = "fecha_fin")
  val fechaFin: Long? = null,

  @ColumnInfo(name = "activa")
  val activa: Boolean = true,

  @ColumnInfo(name = "notas")
  val notas: String? = null,

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
