package com.appcasa.features.family.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.settings.data.local.HogarEntity

/**
 * Representa tanto personas como mascotas del hogar.
 * El campo [tipo] diferencia entre PERSONA, PERRO, GATO, TORTUGA, etc.
 * Los campos específicos de mascota (raza, chip, veterinario) son null para personas.
 */
@Entity(
  tableName = "miembros",
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
data class MiembroEntity(

  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  // TipoMiembro: PERSONA, PERRO, GATO, TORTUGA...
  @ColumnInfo(name = "tipo")
  val tipo: String = TipoMiembro.PERSONA.name,

  @ColumnInfo(name = "fecha_nacimiento")
  val fechaNacimiento: Long? = null,

  @ColumnInfo(name = "foto_uri")
  val fotoUri: String? = null,

  // ── Solo mascotas ──────────────────────────────────
  @ColumnInfo(name = "raza")
  val raza: String? = null,

  @ColumnInfo(name = "color_pelaje")
  val colorPelaje: String? = null,

  @ColumnInfo(name = "numero_chip")
  val numeroChip: String? = null,

  @ColumnInfo(name = "veterinario_nombre")
  val veterinarioNombre: String? = null,

  @ColumnInfo(name = "veterinario_telefono")
  val veterinarioTelefono: String? = null,

  @ColumnInfo(name = "notas")
  val notas: String? = null,

  @ColumnInfo(name = "estado")
  val estado: String = EstadoGeneral.ACTIVO.name,

  @ColumnInfo(name = "sync_id", index = true)
  val syncId: String? = null,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "updated_at")
  val updatedAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "puntos")
  val puntos: Int = 0,

  @ColumnInfo(name = "nivel")
  val nivel: Int = 1
)
