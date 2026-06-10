package com.appcasa.features.dashboard.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.DashboardRemoteDataSource
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.mapper.toDomain
import com.appcasa.features.dashboard.data.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val dashboardDao: DashboardDao,
    private val remoteDataSource: DashboardRemoteDataSource,
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
        updateWidget()
        return id
    }

    override suspend fun deletePostIt(postIt: PostIt) {
        dashboardDao.deletePostIt(postIt.toEntity())
        syncScheduler.scheduleSync(postIt.hogarId)
        updateWidget()
    }

    private fun updateWidget() {
        try {
            val intent = Intent(context, Class.forName("com.appcasa.widget.PostItWidgetProvider"))
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, Class.forName("com.appcasa.widget.PostItWidgetProvider")))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                    remoteDataSource.observePostIts(hogarId)
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val localItem = dashboardDao.getPostItById(remoteItem.id)
                    if (localItem == null || remoteItem.updatedAt > localItem.updatedAt) {
                        dashboardDao.insertPostIt(remoteItem.toEntity())
                        updateWidget()
                    }
                }
            }
            .launchIn(appScope)
    }
}
