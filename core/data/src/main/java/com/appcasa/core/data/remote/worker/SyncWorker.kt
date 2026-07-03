package com.appcasa.core.data.remote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appcasa.core.data.remote.source.*
import com.appcasa.core.domain.repository.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val householdRepository: HouseholdRepository,
  private val tasksRepository: TasksRepository,
  private val financeRepository: FinanceRepository,
  private val familyRepository: FamilyRepository,
  private val inventoryRepository: InventoryRepository,
  private val dashboardRepository: DashboardRepository,
  private val calendarRepository: CalendarRepository,
  private val maintenanceRepository: MaintenanceRepository,
  private val documentRepository: DocumentRepository,
  private val listsRepository: ListsRepository,
  private val petRepository: PetRepository,
  private val taskRemoteDataSource: TaskRemoteDataSource,
  private val financeRemoteDataSource: FinanceRemoteDataSource,
  private val familyRemoteDataSource: FamilyRemoteDataSource,
  private val inventoryRemoteDataSource: InventoryRemoteDataSource,
  private val dashboardRemoteDataSource: DashboardRemoteDataSource,
  private val calendarRemoteDataSource: CalendarRemoteDataSource,
  private val maintenanceRemoteDataSource: MaintenanceRemoteDataSource,
  private val documentRemoteDataSource: DocumentRemoteDataSource,
  private val listRemoteDataSource: ListRemoteDataSource,
  private val petRemoteDataSource: PetRemoteDataSource,
  private val householdRemoteDataSource: HouseholdRemoteDataSource
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    // TODO Phase 4: Event-Based Sync
    return Result.success()
  }
}
