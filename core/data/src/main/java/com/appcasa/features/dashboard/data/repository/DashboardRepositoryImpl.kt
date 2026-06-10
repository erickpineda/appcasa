package com.appcasa.features.dashboard.data.repository

import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.mapper.toDomain
import com.appcasa.features.dashboard.data.mapper.toEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val dashboardDao: DashboardDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val syncManager: SyncManager,
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

    override suspend fun updatePostItSyncTimestamp(postItId: Long) {
        dashboardDao.updateSyncTimestamp(postItId, System.currentTimeMillis())
    }

    private var syncJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: Long) {
        syncJob?.cancel()
        syncJob = syncManager.isAppInForeground
            .flatMapLatest { isInForeground ->
                if (isInForeground) {
                    firestoreDataSource.observePostIts(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val localItem = dashboardDao.getPostItById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        dashboardDao.insertPostIt(remoteItem.toEntity())
                    }
                }
            }
            .launchIn(appScope)
    }
}
