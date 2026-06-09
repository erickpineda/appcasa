package com.appcasa.features.dashboard.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.repository.MaintenanceRepository
import com.appcasa.core.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class MaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val maintenanceRepository: MaintenanceRepository,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val targetId = currentHouseholdProvider.getCurrentHouseholdId().let {
            if (it == 0L) currentHouseholdProvider.householdId.first() else it
        }

        if (targetId == 0L) return Result.success()

        val now = System.currentTimeMillis()
        val tomorrow = now + (24 * 60 * 60 * 1000)
        
        val events = maintenanceRepository.getEventsPaged(targetId, 100, 0).first()
        
        events.filter { it.proximaRevision != null && it.proximaRevision!! in now..tomorrow }
              .forEach { event ->
                  NotificationHelper.showNotification(
                      applicationContext,
                      event.id.toInt() + 1000, // Offset para no colisionar con otros IDs
                      "Mantenimiento: ${event.titulo}",
                      "Tienes una revisión de mantenimiento pendiente próximamente."
                  )
              }
        
        return Result.success()
    }
}
