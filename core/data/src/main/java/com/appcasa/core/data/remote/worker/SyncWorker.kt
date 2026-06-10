package com.appcasa.core.data.remote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.InventoryRepository
import com.appcasa.core.domain.repository.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val financeRepository: FinanceRepository,
    private val familyRepository: FamilyRepository,
    private val inventoryRepository: InventoryRepository,
    private val dashboardRepository: DashboardRepository,
    private val calendarRepository: CalendarRepository,
    private val firestoreDataSource: FirestoreDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hogarId = inputData.getLong("hogarId", -1L)
        if (hogarId == -1L) return Result.failure()

        return try {
            // Sincronizar Tareas que han cambiado
            tasksRepository.getTasksByHogar(hogarId).first().filter { 
                it.updatedAt > (it.lastSyncedAt ?: 0L) 
            }.forEach { task ->
                firestoreDataSource.syncTask(task)
                tasksRepository.updateTaskSyncTimestamp(task.id)
            }
            
            // Sincronizar Gastos que han cambiado
            financeRepository.getExpensesByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { expense ->
                firestoreDataSource.syncExpense(expense)
                financeRepository.updateExpenseSyncTimestamp(expense.id)
            }

            // Sincronizar Miembros que han cambiado
            familyRepository.getMembersByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { member ->
                firestoreDataSource.syncMember(member)
                familyRepository.updateMemberSyncTimestamp(member.id)
            }

            // Sincronizar Inventario que ha cambiado
            inventoryRepository.getStockByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { item ->
                firestoreDataSource.syncStock(item)
                inventoryRepository.updateStockSyncTimestamp(item.id)
            }

            // Sincronizar Post-its que han cambiado
            dashboardRepository.getPostIts(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { postIt ->
                firestoreDataSource.syncPostIt(postIt)
                dashboardRepository.updatePostItSyncTimestamp(postIt.id)
            }

            // Sincronizar Eventos que han cambiado
            calendarRepository.getEventsByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { event ->
                firestoreDataSource.syncEvent(event)
                calendarRepository.updateEventSyncTimestamp(event.id)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
