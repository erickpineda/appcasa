package com.appcasa.features.inventory.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.core.data.local.base.Auditable
import com.appcasa.core.data.local.base.Syncable
import com.appcasa.features.settings.data.local.HogarEntity
import java.util.UUID

/**
 * Representa un artículo del inventario del hogar.
 * Ej: "Pienso Gatos", "Leche", "Detergente".
 */
@Entity(
    tableName = "stock",
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
        Index("nombre")
    ]
)
data class StockEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "categoria")
    val categoria: String, // Mascotas, Despensa, Limpieza, etc.

    @ColumnInfo(name = "cantidad_actual")
    val cantidadActual: Double,

    @ColumnInfo(name = "cantidad_minima")
    val cantidadMinima: Double,

    @ColumnInfo(name = "unidad")
    val unidad: String, // kg, litros, unidades

    @ColumnInfo(name = "auto_comprar")
    val autoComprar: Boolean = true,

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
