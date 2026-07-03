package com.appcasa.features.lists.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import java.util.UUID

@Entity(
  tableName = "lista_items",
  foreignKeys = [ForeignKey(
    entity        = ListaEntity::class,
    parentColumns = ["id"],
    childColumns  = ["lista_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("lista_id")
  ]
)
data class ListaItemEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "lista_id")
  val listaId: String,

  @ColumnInfo(name = "texto")
  val texto: String,

  // Cantidad libre como texto — ej: "2 kg", "3 botes", "1 paquete"
  @ColumnInfo(name = "cantidad")
  val cantidad: String? = null,

  @ColumnInfo(name = "completado")
  val completado: Boolean = false,

  // Orden de visualización dentro de la lista
  @ColumnInfo(name = "orden")
  val orden: Int = 0,

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
