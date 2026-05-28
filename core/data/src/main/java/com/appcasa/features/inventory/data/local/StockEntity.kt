package com.appcasa.features.inventory.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.appcasa.features.settings.data.local.HogarEntity

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
        Index("hogar_id")
    ]
)
data class StockEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "hogar_id")
    val hogarId: Long,

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

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
