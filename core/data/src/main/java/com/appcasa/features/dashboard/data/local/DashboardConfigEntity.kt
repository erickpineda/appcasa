package com.appcasa.features.dashboard.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcasa.features.settings.data.local.HogarEntity

@Entity(
    tableName = "dashboard_config",
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
data class DashboardConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "hogar_id")
    val hogarId: Long,
    
    @ColumnInfo(name = "orden_modulos")
    val ordenModulos: String // Lista separada por comas: "TASKS,PETS,CALENDAR,EXPENSES,POSTITS"
)
