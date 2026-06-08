package com.appcasa.features.dashboard.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.mapper.toDomain
import com.appcasa.features.dashboard.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardDao: DashboardDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val syncScheduler: SyncScheduler
) : DashboardRepository {

    override fun getPostIts(hogarId: Long): Flow<List<PostIt>> {
        return dashboardDao.getPostIts(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPostIt(postIt: PostIt): Long {
        val id = dashboardDao.insertPostIt(postIt.toEntity())
        syncScheduler.scheduleSync(postIt.hogarId)
        return id
    }

    override suspend fun deletePostIt(postIt: PostIt) {
        dashboardDao.deletePostIt(postIt.toEntity())
        syncScheduler.scheduleSync(postIt.hogarId)
    }

    override fun getDashboardConfig(hogarId: Long): Flow<DashboardConfig?> {
        return dashboardDao.getConfig(hogarId).map { it?.toDomain() }
    }

    override suspend fun saveDashboardConfig(config: DashboardConfig) {
        dashboardDao.saveConfig(config.toEntity())
    }

    override fun startRemoteSync(hogarId: Long) {
        firestoreDataSource.observePostIts(hogarId) { remoteItems ->
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.IO) {
                remoteItems.forEach { remoteItem ->
                    val localItem = dashboardDao.getPostItById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        dashboardDao.insertPostIt(remoteItem.toEntity())
                    }
                }
            }
        }
    }
}
