package com.appcasa.features.dashboard.data.repository

import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.mapper.toDomain
import com.appcasa.features.dashboard.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardDao: DashboardDao
) : DashboardRepository {

    override fun getPostIts(hogarId: Long): Flow<List<PostIt>> {
        return dashboardDao.getPostIts(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPostIt(postIt: PostIt): Long {
        return dashboardDao.insertPostIt(postIt.toEntity())
    }

    override suspend fun deletePostIt(postIt: PostIt) {
        dashboardDao.deletePostIt(postIt.toEntity())
    }

    override fun getDashboardConfig(hogarId: Long): Flow<DashboardConfig?> {
        return dashboardDao.getConfig(hogarId).map { it?.toDomain() }
    }

    override suspend fun saveDashboardConfig(config: DashboardConfig) {
        dashboardDao.saveConfig(config.toEntity())
    }
}
