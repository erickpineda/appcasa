package com.appcasa.features.dashboard.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.appcasa.core.data.remote.SyncScheduler
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.data.remote.source.DashboardRemoteDataSource
import com.appcasa.core.data.R
import com.appcasa.core.domain.di.ApplicationScope
import com.appcasa.core.domain.model.DashboardConfig
import com.appcasa.core.domain.model.PostIt
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.utils.NotificationHelper
import com.appcasa.features.dashboard.data.local.DashboardDao
import com.appcasa.features.dashboard.data.mapper.toDomain
import com.appcasa.features.dashboard.data.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.UUID

class DashboardRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val dashboardDao: DashboardDao,
    private val householdRepository: HouseholdRepository,
    private val remoteDataSource: DashboardRemoteDataSource,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler
) : DashboardRepository {

    override fun getPostIts(hogarId: String): Flow<List<PostIt>> {
        return dashboardDao.getPostIts(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertPostIt(postIt: PostIt) {
        dashboardDao.upsertPostIt(postIt.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(postIt.hogarId)
        updateWidget()
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

    override fun getDashboardConfig(hogarId: String): Flow<DashboardConfig?> {
        return dashboardDao.getConfig(hogarId).map { it?.toDomain() }
    }

    override suspend fun saveDashboardConfig(config: DashboardConfig) {
        dashboardDao.upsertConfig(config.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(config.hogarId)
    }

    override suspend fun updateConfigSyncTimestamp(hogarId: String) {
        dashboardDao.updateConfigSyncTimestamp(hogarId, System.currentTimeMillis())
    }

    override suspend fun updatePostItSyncTimestamp(postItId: String) {
        dashboardDao.updateSyncTimestamp(postItId, System.currentTimeMillis())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startRemoteSync(hogarId: String) {
        // TODO Phase 4 Remote Sync
    }
}
