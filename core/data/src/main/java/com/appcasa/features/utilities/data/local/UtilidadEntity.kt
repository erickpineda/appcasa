package com.appcasa.features.utilities.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import java.util.UUID

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

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

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
