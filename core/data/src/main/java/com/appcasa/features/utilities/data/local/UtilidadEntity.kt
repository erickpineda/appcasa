package com.appcasa.features.utilities.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro de módulos y calculadoras disponibles en la app.
 * Permite activar/desactivar utilidades desde la pantalla de ajustes
 * sin necesidad de tocar el código de navegación.
 */
@Entity(
  tableName = "utilidades",
  indices = [Index(value = ["codigo"], unique = true)]
)
data class UtilidadEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  // Identificador único de la utilidad — ej: "CALC_DOSIS"
  @ColumnInfo(name = "codigo")
  val codigo: String,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  // Nombre del icono de Material Design — ej: "medication", "home", "bolt"
  @ColumnInfo(name = "icono")
  val icono: String,

  @ColumnInfo(name = "activa")
  val activa: Boolean = true,

  // Posición en la pantalla de utilidades
  @ColumnInfo(name = "orden")
  val orden: Int = 0,

  @ColumnInfo(name = "categoria")
  val categoria: String = "General",

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
