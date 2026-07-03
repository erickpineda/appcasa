package com.appcasa.features.finance.data.local

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
        Index("fecha")
    ]
)
data class ExpenseEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "hogar_id")
    val hogarId: String,

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

    @ColumnInfo(name = "estado")
    val estado: String = "ACTIVO",

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
