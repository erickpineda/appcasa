package com.appcasa.features.dashboard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM post_its WHERE hogar_id = :hogarId AND deleted_at IS NULL ORDER BY created_at DESC")
    fun getPostIts(hogarId: String): Flow<List<PostItEntity>>

    @Upsert
    suspend fun upsertPostIt(postIt: PostItEntity)

    @Query("SELECT * FROM post_its WHERE id = :id AND deleted_at IS NULL")
    suspend fun getPostItById(id: String): PostItEntity?

    @Query("UPDATE post_its SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    @Query("UPDATE post_its SET deleted_at = :timestamp, deleted_by = :userId WHERE id = :id")
    suspend fun softDeletePostIt(id: String, timestamp: Long, userId: String)

    @Delete
    suspend fun deletePostIt(postIt: PostItEntity)

    @Query("SELECT * FROM dashboard_config WHERE hogar_id = :hogarId AND deleted_at IS NULL")
    fun getConfig(hogarId: String): Flow<DashboardConfigEntity?>

    @Upsert
    suspend fun upsertConfig(config: DashboardConfigEntity)

    @Query("UPDATE dashboard_config SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateConfigSyncTimestamp(id: String, timestamp: Long)
}
