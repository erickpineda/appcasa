package com.appcasa.features.tasks.data.local

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecompensaDao {
    @Query("SELECT * FROM recompensas WHERE hogar_id = :hogarId ORDER BY coste_puntos ASC")
    fun getRecompensasByHogar(hogarId: Long): Flow<List<RecompensaEntity>>

    @Upsert
    suspend fun insertRecompensa(recompensa: RecompensaEntity): Long

    @Update
    suspend fun updateRecompensa(recompensa: RecompensaEntity)

    @Delete
    suspend fun deleteRecompensa(recompensa: RecompensaEntity)
}
