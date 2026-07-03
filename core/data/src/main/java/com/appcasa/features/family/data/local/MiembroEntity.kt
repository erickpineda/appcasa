package com.appcasa.features.family.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.features.settings.data.local.HogarEntity
import java.util.UUID

@Entity(
  tableName = "miembros",
  foreignKeys = [
    ForeignKey(
      entity        = HogarEntity::class,
      parentColumns = ["id"],
      childColumns  = ["hogar_id"],
      onDelete      = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index("hogar_id"),
    Index("firebase_uid")
  ]
)
data class MiembroEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "hogar_id")
  val hogarId: String,

  // Vinculación opcional a una cuenta global de la app
  @ColumnInfo(name = "firebase_uid")
  val firebaseUid: String? = null,

  @ColumnInfo(name = "email")
  val email: String? = null,

  // HUMANO, MASCOTA_PERRO, MASCOTA_GATO, etc.
  @ColumnInfo(name = "tipo")
  val tipo: String = TipoMiembro.PERSONA.name,

  @ColumnInfo(name = "rol")
  val rol: String = RolHogar.COLABORADOR.name,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "avatar_url")
  val avatarUrl: String? = null,

  @ColumnInfo(name = "color_hex")
  val colorHex: String? = null,

  @ColumnInfo(name = "puntos")
  val puntos: Int = 0,

  @ColumnInfo(name = "nivel")
  val nivel: Int = 1,

  @ColumnInfo(name = "estado_animo")
  val estadoAnimo: String? = null,

  @ColumnInfo(name = "fecha_nacimiento")
  val fechaNacimiento: Long? = null,

  @ColumnInfo(name = "notas")
  val notas: String? = null,

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
