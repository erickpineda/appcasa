package com.appcasa.core.domain.usecase.finance

import com.appcasa.core.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

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
