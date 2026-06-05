package com.appcasa.core.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class GetActiveTasksUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Task>> {
        return repository.getTasksPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class GetLowStockItemsUseCase @Inject constructor(
    private val repository: InventoryRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<StockItem>> {
        return repository.getLowStockItems(hogarId)
    }
}

class GetTotalMonthlyExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(hogarId: Long): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return repository.getTotalMonthlyExpense(hogarId, calendar.timeInMillis)
    }
}

class GetFamilyMembersUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<FamilyMember>> {
        return repository.getMembersByHogar(hogarId)
    }
}

class GetActiveListsUseCase @Inject constructor(
    private val repository: ListsRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Lista>> {
        return repository.getListasPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getCurrentUser()
    }
}

class GetConfigurationUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<Configuration>> {
        return repository.getConfiguracion(hogarId)
    }
}

class UpdateConfigurationUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(hogarId: Long, clave: String, valor: String) {
        repository.insertConfiguracion(Configuration(hogarId = hogarId, clave = clave, valor = valor))
    }
}
