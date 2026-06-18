package com.appcasa.features.dashboard.data.local

import androidx.room.*
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM post_its WHERE hogar_id = :hogarId ORDER BY created_at DESC")
    fun getPostIts(hogarId: Long): Flow<List<PostItEntity>>

    @Upsert
    suspend fun insertPostIt(postIt: PostItEntity): Long

    @Query("SELECT * FROM post_its WHERE id = :id")
    suspend fun getPostItById(id: Long): PostItEntity?

    @Query("SELECT * FROM post_its WHERE sync_id = :syncId LIMIT 1")
    suspend fun getPostItBySyncId(syncId: String): PostItEntity?

    @Query("UPDATE post_its SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: Long, timestamp: Long)

    @Query("UPDATE post_its SET hogar_sync_id = :hogarSyncId WHERE id = :id")
    suspend fun updateHogarSyncId(id: Long, hogarSyncId: String)

    @Delete
    suspend fun deletePostIt(postIt: PostItEntity)

    @Query("SELECT * FROM dashboard_config WHERE hogar_id = :hogarId")
    fun getConfig(hogarId: Long): Flow<DashboardConfigEntity?>

    @Upsert
    suspend fun saveConfig(config: DashboardConfigEntity)

    @Query("UPDATE dashboard_config SET last_synced_at = :timestamp WHERE hogar_id = :hogarId")
    suspend fun updateConfigSyncTimestamp(hogarId: Long, timestamp: Long)
}
