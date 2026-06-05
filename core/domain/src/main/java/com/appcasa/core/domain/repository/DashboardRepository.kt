package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getPostIts(hogarId: Long): Flow<List<PostIt>>
    suspend fun insertPostIt(postIt: PostIt): Long
    suspend fun deletePostIt(postIt: PostIt)
    fun getDashboardConfig(hogarId: Long): Flow<DashboardConfig?>
    suspend fun saveDashboardConfig(config: DashboardConfig)
}
