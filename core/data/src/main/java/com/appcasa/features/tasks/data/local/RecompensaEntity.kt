package com.appcasa.features.tasks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
    tableName = "recompensas",
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
data class RecompensaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "hogar_id")
    val hogarId: Long,
    @ColumnInfo(name = "hogar_sync_id")
    val hogarSyncId: String? = null,
    val titulo: String,
    val descripcion: String? = null,
    @ColumnInfo(name = "coste_puntos")
    val costePuntos: Int,
    val icono: String = "card_giftcard", // Nombre del icono de Material
    
    @ColumnInfo(name = "sync_id")
    val syncId: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
)
