package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getPostIts(hogarId: String): Flow<List<PostIt>>
    suspend fun upsertPostIt(postIt: PostIt)
    suspend fun deletePostIt(postIt: PostIt)
    fun getDashboardConfig(hogarId: String): Flow<DashboardConfig?>
    suspend fun saveDashboardConfig(config: DashboardConfig)
    suspend fun updateConfigSyncTimestamp(hogarId: String)
    
    suspend fun updatePostItSyncTimestamp(postItId: String)
    fun startRemoteSync(hogarId: String)
}
