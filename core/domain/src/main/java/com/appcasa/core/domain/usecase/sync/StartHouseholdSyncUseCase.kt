package com.appcasa.core.domain.usecase.sync

import com.appcasa.core.domain.repository.DashboardRepository
import com.appcasa.core.domain.repository.DocumentRepository
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.InventoryRepository
import com.appcasa.core.domain.repository.MaintenanceRepository
import com.appcasa.core.domain.repository.TasksRepository
import javax.inject.Inject

class StartHouseholdSyncUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val financeRepository: FinanceRepository,
    private val familyRepository: FamilyRepository,
    private val inventoryRepository: InventoryRepository,
    private val dashboardRepository: DashboardRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(householdId: Long) {
        if (householdId != 0L) {
            tasksRepository.startRemoteSync(householdId)
            financeRepository.startRemoteSync(householdId)
            familyRepository.startRemoteSync(householdId)
            inventoryRepository.startRemoteSync(householdId)
            dashboardRepository.startRemoteSync(householdId)
            maintenanceRepository.startRemoteSync(householdId)
            documentRepository.startRemoteSync(householdId)
        }
    }
}
