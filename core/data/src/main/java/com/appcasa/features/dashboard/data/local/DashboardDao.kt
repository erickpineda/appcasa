package com.appcasa.features.dashboard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM post_its WHERE hogar_id = :hogarId ORDER BY created_at DESC")
    fun getPostIts(hogarId: Long): Flow<List<PostItEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostIt(postIt: PostItEntity): Long

    @Delete
    suspend fun deletePostIt(postIt: PostItEntity)

    @Query("SELECT * FROM dashboard_config WHERE hogar_id = :hogarId")
    fun getConfig(hogarId: Long): Flow<DashboardConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: DashboardConfigEntity)
}
