package com.appcasa.features.family.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MiembroDao {
    @Query("SELECT * FROM miembros WHERE hogar_id = :hogarId AND deleted_at IS NULL")
    fun getMiembrosByHogar(hogarId: String): Flow<List<MiembroEntity>>

    @Query("SELECT * FROM miembros WHERE id = :id AND deleted_at IS NULL")
    suspend fun getMiembroById(id: String): MiembroEntity?

    @Query("UPDATE miembros SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    @Upsert
    suspend fun upsertMiembro(miembro: MiembroEntity)

    @Query("UPDATE miembros SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeleteMiembro(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deleteMiembro(miembro: MiembroEntity)

    @Query("DELETE FROM miembros")
    suspend fun deleteAll()
}
