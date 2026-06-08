package com.appcasa.features.dashboard.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
    tableName = "post_its",
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
        Index("sync_id")
    ]
)
data class PostItEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "hogar_id")
    val hogarId: Long,
    
    @ColumnInfo(name = "contenido")
    val contenido: String,
    
    @ColumnInfo(name = "color_hex")
    val colorHex: String = "#FFF9C4", // Amarillo post-it por defecto
    
    @ColumnInfo(name = "sync_id")
    val syncId: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
