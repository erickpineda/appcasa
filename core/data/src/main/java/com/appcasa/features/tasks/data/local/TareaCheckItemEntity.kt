package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Subtareas o checklist dentro de una tarea.
 * Ej: "Tarea: Limpiar cocina" → items: ["Fregar suelo", "Limpiar encimera", "Sacar basura"]
 */
@Entity(
  tableName = "tarea_check_items",
  foreignKeys = [ForeignKey(
    entity        = TareaEntity::class,
    parentColumns = ["id"],
    childColumns  = ["tarea_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [Index("tarea_id")]
)
data class TareaCheckItemEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "tarea_id")
  val tareaId: Long,

  @ColumnInfo(name = "texto")
  val texto: String,

  @ColumnInfo(name = "completado")
  val completado: Boolean = false,

  // Posición del item dentro del checklist
  @ColumnInfo(name = "orden")
  val orden: Int = 0,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis()
)
