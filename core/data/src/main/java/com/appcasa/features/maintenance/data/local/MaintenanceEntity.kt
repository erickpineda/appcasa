package com.appcasa.features.maintenance.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "hogar_id")
    val hogarId: Long,
    
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
    val fotosJson: String? = null // Lista de URIs en formato JSON
)
