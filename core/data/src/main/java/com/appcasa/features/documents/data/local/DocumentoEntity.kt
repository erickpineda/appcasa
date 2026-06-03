package com.appcasa.features.documents.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

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
    Index("categoria"),
    Index("sync_id")
  ]
)
data class DocumentoEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "hogar_id")
  val hogarId: Long,

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

  @ColumnInfo(name = "sync_id")
  val syncId: String? = null,

  @ColumnInfo(name = "sincronizado")
  val sincronizado: Boolean = false,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis()
)
