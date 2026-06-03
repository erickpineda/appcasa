package com.appcasa.features.settings.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.RolHogar

@Entity(
  tableName = "usuarios",
  foreignKeys = [ForeignKey(
    entity        = HogarEntity::class,
    parentColumns = ["id"],
    childColumns  = ["hogar_id"],
    onDelete      = ForeignKey.CASCADE
  )],
  indices = [
    Index("hogar_id"),
    Index("email", unique = true),
    Index("auth_id"),
    Index("sync_id")
  ]
)
data class UsuarioEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "email")
  val email: String,

  @ColumnInfo(name = "avatar_url")
  val avatarUrl: String? = null,

  // ID único del proveedor de autenticación (Firebase/Supabase)
  @ColumnInfo(name = "auth_id")
  val authId: String? = null,

  @ColumnInfo(name = "rol")
  val rol: String = RolHogar.COLABORADOR.name,

  @ColumnInfo(name = "estado")
  val estado: String = EstadoGeneral.ACTIVO.name,

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "is_active")
  val isActive: Boolean = false,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis()
)
