package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
  tableName = "categorias_tarea",
  foreignKeys = [ForeignKey(
    entity        = HogarEntity::class,
    parentColumns = ["id"],
    childColumns  = ["hogar_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("hogar_id"),
    Index("sync_id")
  ]
)
data class CategoriaTareaEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  @ColumnInfo(name = "hogar_sync_id")
  val hogarSyncId: String? = null,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  // Color en hex sin # — ej: "2563EB"
  @ColumnInfo(name = "color_hex")
  val colorHex: String = "2563EB",

  // Nombre del icono de Material Design — ej: "cleaning_services"
  @ColumnInfo(name = "icono")
  val icono: String? = null,

  @ColumnInfo(name = "orden")
  val orden: Int = 0,

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
