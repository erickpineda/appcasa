package com.appcasa.features.finance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.data.local.ExpenseEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
  private val expenseDao: ExpenseDao,
  private val configuracionDao: ConfiguracionDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val expenses: StateFlow<List<ExpenseEntity>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> expenseDao.getExpensesByHogar(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currencySymbol: StateFlow<String> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      configuracionDao.getConfiguracion(id)
        .map { list -> list.find { it.clave == "moneda" }?.valor ?: "€" }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

  fun addExpense(concepto: String, importe: Double, categoria: String) {
    viewModelScope.launch {
      expenseDao.insertExpense(
        ExpenseEntity(
          hogarId = householdId,
          concepto = concepto,
          importe = importe,
          categoria = categoria
        )
      )
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      expenseDao.deleteExpense(expense)
    }
  }

  fun updateExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      expenseDao.insertExpense(expense)
    }
  }
}
