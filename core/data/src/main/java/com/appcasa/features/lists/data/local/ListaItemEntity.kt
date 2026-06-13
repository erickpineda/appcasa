package com.appcasa.features.lists.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "lista_items",
  foreignKeys = [ForeignKey(
    entity        = ListaEntity::class,
    parentColumns = ["id"],
    childColumns  = ["lista_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("lista_id"),
    Index("sync_id")
  ]
)
data class ListaItemEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "lista_id")
  val listaId: Long,

  @ColumnInfo(name = "lista_sync_id")
  val listaSyncId: String? = null,

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

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
