package com.appcasa.features.settings.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.core.domain.model.EstadoGeneral
import java.util.UUID

@Entity(
  tableName = "usuarios",
  indices = [
    Index("email", unique = true),
    Index("auth_id")
  ]
)
data class UsuarioEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  // ID único del proveedor de autenticación (Firebase/Supabase)
  @ColumnInfo(name = "auth_id")
  val authId: String? = null,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "email")
  val email: String,

  @ColumnInfo(name = "avatar_url")
  val avatarUrl: String? = null,

  @ColumnInfo(name = "estado")
  val estado: String = EstadoGeneral.ACTIVO.name,

  @ColumnInfo(name = "created_at")
  override val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  override val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "hogar_id")
  val hogarId: String? = null,

  @ColumnInfo(name = "miembro_id")
  val miembroId: String? = null,

  @ColumnInfo(name = "created_by")
  override val createdBy: String? = null,

  @ColumnInfo(name = "updated_by")
  override val updatedBy: String? = null,

  @ColumnInfo(name = "deleted_at")
  override val deletedAt: Long? = null,

  @ColumnInfo(name = "deleted_by")
  override val deletedBy: String? = null,

  @ColumnInfo(name = "last_synced_at")
  override var lastSyncedAt: Long? = null

) : Syncable, Auditable
