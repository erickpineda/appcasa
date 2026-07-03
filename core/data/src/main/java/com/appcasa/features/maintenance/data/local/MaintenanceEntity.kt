package com.appcasa.features.maintenance.data.local

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
    tableName = "mantenimiento_hogar",
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
        Index("fecha_realizacion"),
        Index("proxima_revision")
    ]
)
data class MaintenanceEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "hogar_id")
    val hogarId: String,
    
    @ColumnInfo(name = "titulo")
    val titulo: String,
    
    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,
    
    @ColumnInfo(name = "categoria")
    val categoria: String, // Electrodomésticos, Fontanería, Electricidad, Pintura, Jardín, etc.
    
    @ColumnInfo(name = "fecha_realizacion")
    val fechaRealizacion: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "proxima_revision")
    val proximaRevision: Long? = null,
    
    @ColumnInfo(name = "coste")
    val coste: Double? = null,
    
    @ColumnInfo(name = "fotos_json")
    val fotosJson: String? = null, // Lista de URIs en formato JSON

    @ColumnInfo(name = "archived")
    val archived: Boolean = false,

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
