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

    override fun getPostIts(hogarId: Long): Flow<List<PostIt>> {
        return dashboardDao.getPostIts(hogarId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPostIt(postIt: PostIt): Long {
        var postItToInsert = postIt
        if (postItToInsert.hogarSyncId == null && postItToInsert.hogarId > 0) {
            val hogar = householdRepository.getHogarById(postItToInsert.hogarId).first()
            postItToInsert = postItToInsert.copy(hogarSyncId = hogar?.syncId)
        }
        if (postItToInsert.syncId == null) {
            postItToInsert = postItToInsert.copy(syncId = UUID.randomUUID().toString())
        }
        val existing = postItToInsert.syncId?.let { dashboardDao.getPostItBySyncId(it) }
        if (existing != null) {
            postItToInsert = postItToInsert.copy(id = existing.id)
        }
        val id = dashboardDao.insertPostIt(postItToInsert.copy(updatedAt = System.currentTimeMillis()).toEntity())
        syncScheduler.scheduleSync(postItToInsert.hogarId)
        updateWidget()
        return id
    }

    override suspend fun deletePostIt(postIt: PostIt) {
        dashboardDao.deletePostIt(postIt.toEntity())
        try {
            remoteDataSource.deletePostIt(postIt)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        var configToSave = config
        if (configToSave.hogarSyncId == null && configToSave.hogarId > 0) {
            val hogar = householdRepository.getHogarById(configToSave.hogarId).first()
            configToSave = configToSave.copy(hogarSyncId = hogar?.syncId)
        }
        dashboardDao.saveConfig(configToSave.toEntity())
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
                    val hogar = householdRepository.getHogarById(hogarId).first()
                    hogar?.syncId?.let { remoteDataSource.observePostIts(it) } ?: emptyFlow()
                } else {
                    emptyFlow()
                }
            }
            .onEach { remoteItems ->
                remoteItems.forEach { remoteItem ->
                    val existing = remoteItem.syncId?.let { dashboardDao.getPostItBySyncId(it) }
                    val hogar = householdRepository.getHogarById(hogarId).first()

                    val postItToSave = remoteItem.copy(
                        id = existing?.id ?: 0L,
                        hogarId = hogarId,
                        hogarSyncId = hogar?.syncId,
                        lastSyncedAt = System.currentTimeMillis()
                    )

                    if (existing == null) {
                        dashboardDao.insertPostIt(postItToSave.toEntity())
                        updateWidget()
                        NotificationHelper.showNotification(
                            context,
                            postItToSave.syncId.hashCode(),
                            context.getString(R.string.notif_new_postit_title),
                            postItToSave.contenido.take(50)
                        )
                    } else if (remoteItem.updatedAt > existing.updatedAt) {
                        dashboardDao.insertPostIt(postItToSave.toEntity())
                        updateWidget()
                    }
                }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(appScope)
    }
}
