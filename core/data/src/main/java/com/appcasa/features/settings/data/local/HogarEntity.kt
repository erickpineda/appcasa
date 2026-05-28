package com.appcasa.features.settings.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.EstadoGeneral

@Entity(tableName = "hogares")
data class HogarEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "descripcion")
  val descripcion: String? = null,

  @ColumnInfo(name = "estado")
  val estado: String = EstadoGeneral.ACTIVO.name,

  // UUID para sincronización con Supabase. Null hasta primera sync.
  @ColumnInfo(name = "sync_id", index = true)
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)
