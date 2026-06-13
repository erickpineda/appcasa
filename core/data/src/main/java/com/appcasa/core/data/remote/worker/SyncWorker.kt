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
    private val petRemoteDataSource: PetRemoteDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hogarId = inputData.getLong("hogarId", -1L)
        if (hogarId == -1L) return Result.failure()

        val hogar = householdRepository.getHogarById(hogarId).first()
        val hogarSyncId = hogar?.syncId ?: return Result.failure()

        return try {
            // Sincronizar Tareas que han cambiado
            tasksRepository.getTasksByHogar(hogarId).first().filter { 
                it.updatedAt > (it.lastSyncedAt ?: 0L) 
            }.forEach { task ->
                taskRemoteDataSource.syncTask(task)
                tasksRepository.updateTaskSyncTimestamp(task.id)
            }

            // Sincronizar check-items de tareas que han cambiado
            tasksRepository.getCheckItemsToSync(hogarId).forEach { item ->
                taskRemoteDataSource.syncCheckItem(hogarSyncId, item)
                tasksRepository.updateCheckItemSyncTimestamp(item.id)
            }
                
            // Sincronizar asignaciones
            tasksRepository.getTasksByHogar(hogarId).first().forEach { task ->
                tasksRepository.getAssignmentsForTask(task.id).first().filter {
                    it.updatedAt > (it.lastSyncedAt ?: 0L)
                }.forEach { assignment ->
                    taskRemoteDataSource.syncAssignment(hogarSyncId, assignment)
                }
            }
            
            // Sincronizar Gastos que han cambiado
            financeRepository.getExpensesByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { expense ->
                financeRemoteDataSource.syncExpense(expense)
                financeRepository.updateExpenseSyncTimestamp(expense.id)
            }

            // Sincronizar Miembros que han cambiado
            familyRepository.getMembersByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { member ->
                familyRemoteDataSource.syncMember(member)
                familyRepository.updateMemberSyncTimestamp(member.id)
            }

            // Sincronizar Inventario que ha cambiado
            inventoryRepository.getStockByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { item ->
                inventoryRemoteDataSource.syncStock(item)
                inventoryRepository.updateStockSyncTimestamp(item.id)
            }

            // Sincronizar Post-its que han cambiado
            dashboardRepository.getPostIts(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { postIt ->
                dashboardRemoteDataSource.syncPostIt(postIt)
                dashboardRepository.updatePostItSyncTimestamp(postIt.id)
            }

            // Sincronizar Eventos que han cambiado
            calendarRepository.getEventsByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { event ->
                calendarRemoteDataSource.syncEvent(event)
                calendarRepository.updateEventSyncTimestamp(event.id)
            }

            // Sincronizar Mantenimiento que ha cambiado
            maintenanceRepository.getEventsPaged(hogarId, 100, 0).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { event ->
                maintenanceRemoteDataSource.syncMaintenance(event)
                maintenanceRepository.updateMaintenanceSyncTimestamp(event.id)
            }

            // Sincronizar Documentos que han cambiado
            documentRepository.getDocumentosByHogar(hogarId).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { doc ->
                documentRemoteDataSource.syncDocument(doc)
                documentRepository.updateDocumentSyncTimestamp(doc.id)
            }

            // Sincronizar Listas que han cambiado
            listsRepository.getListasPaged(hogarId, 100, 0).first().filter {
                it.updatedAt > (it.lastSyncedAt ?: 0L)
            }.forEach { list ->
                listRemoteDataSource.syncList(list)
                listsRepository.updateListSyncTimestamp(list.id)
            }

            // Sincronizar ítems de listas que han cambiado
            listsRepository.getItemsToSync(hogarId).forEach { item ->
                listRemoteDataSource.syncListItem(hogarSyncId, item)
                listsRepository.updateListItemSyncTimestamp(item.id)
            }

            // --- Sincronización de Mascotas (Pesos, Vacunas, Medicinas, Desparasitaciones) ---
            
            petRepository.getWeightsToSync(hogarId).forEach { weight ->
                petRemoteDataSource.syncWeight(hogarSyncId, weight)
                petRepository.updateWeightSyncTimestamp(weight.id)
            }
            
            petRepository.getVaccinesToSync(hogarId).forEach { vaccine ->
                petRemoteDataSource.syncVaccine(hogarSyncId, vaccine)
                petRepository.updateVaccineSyncTimestamp(vaccine.id)
            }
            
            petRepository.getMedicationsToSync(hogarId).forEach { med ->
                petRemoteDataSource.syncMedication(hogarSyncId, med)
                petRepository.updateMedicationSyncTimestamp(med.id)
            }
            
            petRepository.getDewormingsToSync(hogarId).forEach { deworming ->
                petRemoteDataSource.syncDeworming(hogarSyncId, deworming)
                petRepository.updateDewormingSyncTimestamp(deworming.id)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
