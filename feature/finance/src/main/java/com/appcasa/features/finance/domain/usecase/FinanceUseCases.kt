package com.appcasa.features.finance.domain.usecase

import com.appcasa.core.domain.model.Expense
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Expense>> {
        return repository.getExpensesPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class GetArchivedExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Expense>> {
        return repository.getArchivedExpensesPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class AddExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(hogarId: Long, concepto: String, importe: Double, categoria: String, fotoUri: String? = null) {
        val currentUser = userRepository.getCurrentUser().first()
        repository.insertExpense(
            Expense(
                hogarId = hogarId,
                concepto = concepto,
                importe = importe,
                categoria = categoria,
                fotoUri = fotoUri,
                createdById = currentUser?.id
            )
        )
    }
}

class DeleteExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.deleteExpense(expense)
    }
}

class UnarchiveExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(expenseId: Long) {
        repository.unarchiveExpense(expenseId)
    }
}

class ClearAllArchivedExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        repository.deleteAllExpenses(hogarId)
    }
}

class ArchiveOldExpensesUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        repository.archiveOldExpenses(hogarId, threshold)
    }
}

class PurgeOldPhotosUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        repository.purgeOldExpensePhotos(hogarId, threshold)
    }
}

class UpdateExpenseUseCase @Inject constructor(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.insertExpense(expense)
    }
}
