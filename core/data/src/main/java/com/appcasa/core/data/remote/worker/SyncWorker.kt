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
    val hogarId = inputData.getLong("hogarId", -1L)
    if (hogarId == -1L) return Result.failure()

    val hogar = householdRepository.getHogarById(hogarId).first() ?: return Result.failure()
    val hogarSyncId = hogar.syncId ?: return Result.failure()

    return try {
      // Sincronizar el Hogar si no ha sido sincronizado o ha cambiado
      try {
        if (hogar.lastSyncedAt == null || hogar.updatedAt > (hogar.lastSyncedAt ?: 0L)) {
          householdRemoteDataSource.syncHousehold(hogar)
          householdRepository.updateHogarSyncTimestamp(hogarId, System.currentTimeMillis())
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Tareas que han cambiado
      try {
        tasksRepository.getTasksByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { task ->
          if (task.syncId == null) {
            tasksRepository.insertTask(task)
          } else {
            taskRemoteDataSource.syncTask(hogarSyncId, task)
            tasksRepository.updateTaskSyncTimestamp(task.id)
            if (task.hogarSyncId == null) {
              tasksRepository.updateTaskHogarSyncId(task.id, hogarSyncId)
            }
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar check-items de tareas que han cambiado
      try {
        tasksRepository.getCheckItemsToSync(hogarId).forEach { item ->
          if (item.syncId == null) {
            tasksRepository.insertCheckItem(item)
          } else {
            taskRemoteDataSource.syncCheckItem(hogarSyncId, item)
            tasksRepository.updateCheckItemSyncTimestamp(item.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar asignaciones
      try {
        tasksRepository.getTasksByHogar(hogarId).first().forEach { task ->
          tasksRepository.getAssignmentsForTask(task.id).first().filter {
            it.updatedAt > (it.lastSyncedAt ?: 0L)
          }.forEach { assignment ->
            if (assignment.syncId == null) {
              tasksRepository.insertAssignment(assignment)
            } else {
              taskRemoteDataSource.syncAssignment(hogarSyncId, assignment)
            }
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Recompensas
      try {
        tasksRepository.getRewardsByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { reward ->
          if (reward.syncId == null) {
            tasksRepository.insertReward(reward)
          } else {
            taskRemoteDataSource.syncReward(hogarSyncId, reward)
            tasksRepository.updateRewardSyncTimestamp(reward.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Categorías de Tareas
      try {
        tasksRepository.getCategoriesByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { category ->
          if (category.syncId == null) {
            tasksRepository.insertCategory(category)
          } else {
            taskRemoteDataSource.syncCategory(hogarSyncId, category)
            tasksRepository.updateCategorySyncTimestamp(category.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Configuración del Dashboard
      try {
        val config = dashboardRepository.getDashboardConfig(hogarId).first()
        if (config != null && config.updatedAt > (config.lastSyncedAt ?: 0L)) {
          dashboardRemoteDataSource.syncConfig(hogarSyncId, config)
          dashboardRepository.updateConfigSyncTimestamp(hogarId)
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Gastos que han cambiado
      try {
        financeRepository.getExpensesByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { expense ->
          if (expense.syncId == null) {
            financeRepository.insertExpense(expense)
          } else {
            financeRemoteDataSource.syncExpense(hogarSyncId, expense)
            financeRepository.updateExpenseSyncTimestamp(expense.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Miembros que han cambiado
      try {
        familyRepository.getMembersByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { member ->
          if (member.syncId == null) {
            familyRepository.insertMember(member)
          } else {
            familyRemoteDataSource.syncMember(hogarSyncId, member)
            familyRepository.updateMemberSyncTimestamp(member.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Inventario que ha cambiado
      try {
        inventoryRepository.getStockByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { item ->
          if (item.syncId == null) {
            inventoryRepository.insertStockItem(item)
          } else {
            inventoryRemoteDataSource.syncStock(hogarSyncId, item)
            inventoryRepository.updateStockSyncTimestamp(item.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Post-its que han cambiado
      try {
        dashboardRepository.getPostIts(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { postIt ->
          if (postIt.syncId == null) {
            dashboardRepository.insertPostIt(postIt)
          } else {
            dashboardRemoteDataSource.syncPostIt(hogarSyncId, postIt)
            dashboardRepository.updatePostItSyncTimestamp(postIt.id)
            if (postIt.hogarSyncId == null) {
              dashboardRepository.updatePostItHogarSyncId(postIt.id, hogarSyncId)
            }
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Eventos que han cambiado
      try {
        calendarRepository.getEventsByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { event ->
          if (event.syncId == null) {
            calendarRepository.insertEvent(event)
          } else {
            calendarRemoteDataSource.syncEvent(hogarSyncId, event)
            calendarRepository.updateEventSyncTimestamp(event.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Mantenimiento que ha cambiado
      try {
        maintenanceRepository.getEventsPaged(hogarId, 100, 0).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { event ->
          if (event.syncId == null) {
            maintenanceRepository.insertEvent(event)
          } else {
            maintenanceRemoteDataSource.syncMaintenance(hogarSyncId, event)
            maintenanceRepository.updateMaintenanceSyncTimestamp(event.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Documentos que han cambiado
      try {
        documentRepository.getDocumentosByHogar(hogarId).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { doc ->
          if (doc.syncId == null) {
            documentRepository.insertDocumento(doc)
          } else {
            documentRemoteDataSource.syncDocument(hogarSyncId, doc)
            documentRepository.updateDocumentSyncTimestamp(doc.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar Listas que han cambiado
      try {
        listsRepository.getListasPaged(hogarId, 100, 0).first().filter {
          it.updatedAt > (it.lastSyncedAt ?: 0L)
        }.forEach { list ->
          if (list.syncId == null) {
            listsRepository.insertLista(list)
          } else {
            listRemoteDataSource.syncList(hogarSyncId, list)
            listsRepository.updateListSyncTimestamp(list.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // Sincronizar ítems de listas que han cambiado
      try {
        listsRepository.getItemsToSync(hogarId).forEach { item ->
          if (item.syncId == null) {
            listsRepository.insertItem(item)
          } else {
            listRemoteDataSource.syncListItem(hogarSyncId, item)
            listsRepository.updateListItemSyncTimestamp(item.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      // --- Sincronización de Mascotas (Pesos, Vacunas, Medicinas, Desparasitaciones) ---
      try {
        petRepository.getWeightsToSync(hogarId).forEach { weight ->
          if (weight.syncId == null) {
            petRepository.insertPeso(weight)
          } else {
            petRemoteDataSource.syncWeight(hogarSyncId, weight)
            petRepository.updateWeightSyncTimestamp(weight.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      try {
        petRepository.getVaccinesToSync(hogarId).forEach { vaccine ->
          if (vaccine.syncId == null) {
            petRepository.insertVacuna(vaccine)
          } else {
            petRemoteDataSource.syncVaccine(hogarSyncId, vaccine)
            petRepository.updateVaccineSyncTimestamp(vaccine.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      try {
        petRepository.getMedicationsToSync(hogarId).forEach { med ->
          if (med.syncId == null) {
            petRepository.insertMedicacion(med)
          } else {
            petRemoteDataSource.syncMedication(hogarSyncId, med)
            petRepository.updateMedicationSyncTimestamp(med.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      try {
        petRepository.getDewormingsToSync(hogarId).forEach { deworming ->
          if (deworming.syncId == null) {
            petRepository.insertDesparasitacion(deworming)
          } else {
            petRemoteDataSource.syncDeworming(hogarSyncId, deworming)
            petRepository.updateDewormingSyncTimestamp(deworming.id)
          }
        }
      } catch (e: Exception) { e.printStackTrace() }

      Result.success()
    } catch (e: Exception) {
      Result.retry()
    }
  }
}
