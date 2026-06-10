package com.appcasa.features.family.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MiembroDao {
    @Query("SELECT * FROM miembros WHERE hogar_id = :hogarId")
    fun getMiembrosByHogar(hogarId: Long): Flow<List<MiembroEntity>>

    @Query("SELECT * FROM miembros WHERE id = :id")
    suspend fun getMiembroById(id: Long): MiembroEntity?

    @Query("UPDATE miembros SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: Long, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiembro(miembro: MiembroEntity): Long

    @Update
    suspend fun updateMiembro(miembro: MiembroEntity)

    @Delete
    suspend fun deleteMiembro(miembro: MiembroEntity)

    @Query("DELETE FROM miembros")
    suspend fun deleteAll()
}
