package com.appcasa.features.documents.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.features.settings.data.local.HogarEntity
import java.util.UUID

@Entity(
  tableName = "documentos",
  foreignKeys = [
    ForeignKey(
      entity = HogarEntity::class,
      parentColumns = ["id"],
      childColumns = ["hogar_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index("hogar_id"),
    Index("categoria")
  ]
)
data class DocumentoEntity(

  @PrimaryKey
  @ColumnInfo(name = "id")
  override val id: String = UUID.randomUUID().toString(),

  @ColumnInfo(name = "hogar_id")
  val hogarId: String,

  @ColumnInfo(name = "nombre")
  val nombre: String,

  @ColumnInfo(name = "categoria")
  val categoria: String, // Escolares, Garantías, Salud, Seguros, etc.

  @ColumnInfo(name = "uri_pdf")
  val uriPdf: String,

  @ColumnInfo(name = "fecha_vencimiento")
  val fechaVencimiento: Long? = null,

  @ColumnInfo(name = "url_nube")
  val urlNube: String? = null,

  @ColumnInfo(name = "sincronizado")
  val sincronizado: Boolean = false,

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
