package com.appcasa.features.lists.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.TipoLista
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
  tableName = "listas",
  foreignKeys = [ForeignKey(
    entity        = HogarEntity::class,
    parentColumns = ["id"],
    childColumns  = ["hogar_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("hogar_id"),
    Index("tipo")
  ]
)
data class ListaEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  // TipoLista: COMPRA, FARMACIA, VETERINARIO, VIAJE, ESCOLAR, PERSONALIZADA
  @ColumnInfo(name = "tipo")
  val tipo: String = TipoLista.PERSONALIZADA.name,

  @ColumnInfo(name = "completada")
  val completada: Boolean = false,

  @ColumnInfo(name = "sync_id", index = true)
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)
