package com.appcasa.features.calendar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
  tableName = "eventos",
  foreignKeys = [ForeignKey(
    entity        = HogarEntity::class,
    parentColumns = ["id"],
    childColumns  = ["hogar_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("hogar_id"),
    Index("fecha"),
    Index("tipo"),
    Index("sync_id")
  ]
)
data class EventoEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  @ColumnInfo(name = "titulo")
  val titulo: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  // TipoEvento: CUMPLEANOS, CITA_VETERINARIO, VACUNA, ITV, SEGURO...
  @ColumnInfo(name = "tipo")
  val tipo: String = TipoEvento.OTRO.name,

  @ColumnInfo(name = "fecha")
  val fecha: Long,

  @ColumnInfo(name = "fecha_fin")
  val fechaFin: Long? = null,

  // ID del miembro asociado al evento (ej: cumpleaños del hijo, vacuna de Luna)
  @ColumnInfo(name = "miembro_id")
  val miembroId: Long? = null,

  @ColumnInfo(name = "todo_el_dia")
  val todoElDia: Boolean = true,

  // Cumpleaños, aniversarios — se repiten automáticamente cada año
  @ColumnInfo(name = "repeticion_anual")
  val repeticionAnual: Boolean = false,

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
