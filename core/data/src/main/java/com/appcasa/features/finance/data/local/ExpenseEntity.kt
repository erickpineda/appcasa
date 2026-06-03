package com.appcasa.features.finance.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

/**
 * Representa un gasto familiar registrado.
 * Ej: "25€ - Supermercado", "Vete bobby".
 */
@Entity(
    tableName = "gastos",
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
        Index("fecha"),
        Index("created_by_id"),
        Index("sync_id")
    ]
)
data class ExpenseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "hogar_id")
    val hogarId: Long,

    @ColumnInfo(name = "concepto")
    val concepto: String,

    @ColumnInfo(name = "importe")
    val importe: Double,

    @ColumnInfo(name = "categoria")
    val categoria: String, // Comida, Mascotas, Vivienda, Ocio, etc.

    @ColumnInfo(name = "fecha")
    val fecha: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "foto_uri")
    val fotoUri: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_by_id")
    val createdById: Long? = null,

    @ColumnInfo(name = "sync_id")
    val syncId: String? = null,

    @ColumnInfo(name = "archived")
    val archived: Boolean = false
)
