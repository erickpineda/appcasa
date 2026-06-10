package com.appcasa.features.settings.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.TipoConfiguracion

@Entity(
  tableName = "configuracion",
  foreignKeys = [ForeignKey(
    entity        = HogarEntity::class,
    parentColumns = ["id"],
    childColumns  = ["hogar_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [Index(value = ["hogar_id", "clave"], unique = true)]
)
data class ConfiguracionEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  // Ejemplos de clave: "tema_oscuro", "dias_aviso_vacuna", "notif_activas"
  @ColumnInfo(name = "clave")
  val clave: String,

  @ColumnInfo(name = "valor")
  val valor: String,

  @ColumnInfo(name = "tipo")
  val tipo: String = TipoConfiguracion.STRING.name,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "last_synced_at")
  val lastSyncedAt: Long? = null
)
