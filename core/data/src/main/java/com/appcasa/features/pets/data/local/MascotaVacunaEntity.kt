package com.appcasa.features.pets.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.family.data.local.MiembroEntity

@Entity(
  tableName = "mascota_vacunas",
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
data class MascotaVacunaEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "mascota_id")
  val mascotaId: Long,

  // Nombre de la vacuna — ej: "Rabia", "Leucemia felina", "Parvovirus"
  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "fecha_aplicacion")
  val fechaAplicacion: Long,

  // Null si no hay próxima dosis programada
  @ColumnInfo(name = "fecha_proxima")
  val fechaProxima: Long? = null,

  @ColumnInfo(name = "veterinario")
  val veterinario: String? = null,

  @ColumnInfo(name = "notas")
  val notas: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis()
)
