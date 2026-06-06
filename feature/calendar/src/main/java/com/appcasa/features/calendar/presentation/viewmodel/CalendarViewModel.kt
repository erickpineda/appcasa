package com.appcasa.features.calendar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.calendar.domain.usecase.CalendarState
import com.appcasa.features.calendar.domain.usecase.GetCalendarItemsUseCase
import com.appcasa.features.calendar.domain.usecase.AddEventUseCase
import com.appcasa.features.calendar.domain.usecase.UpdateEventUseCase
import com.appcasa.features.calendar.domain.usecase.DeleteEventUseCase
import com.appcasa.features.calendar.domain.usecase.ImportShiftsFromCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
  private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
  private val addEventUseCase: AddEventUseCase,
  private val updateEventUseCase: UpdateEventUseCase,
  private val deleteEventUseCase: DeleteEventUseCase,
  private val importShiftsFromCsvUseCase: ImportShiftsFromCsvUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _historyPage = MutableStateFlow(0)
  val historyPage = _historyPage.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val calendarItems: StateFlow<CalendarState> = combine(
    currentHouseholdProvider.householdId,
    _searchQuery
  ) { householdId, query -> 
    householdId to query
  }
    .flatMapLatest { (householdId, query) ->
      getCalendarItemsUseCase(householdId, query)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = CalendarState()
    )

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  fun loadMoreHistory() {
    _historyPage.value += 1
  }

  fun addEvento(titulo: String, fecha: Long, tipo: TipoEvento = TipoEvento.OTRO) {
    viewModelScope.launch {
      addEventUseCase(currentHouseholdProvider.getCurrentHouseholdId(), titulo, fecha, tipo)
    }
  }

  fun updateEvento(event: Event) {
    viewModelScope.launch {
      updateEventUseCase(event)
    }
  }

  fun deleteEvento(event: Event) {
    viewModelScope.launch {
      deleteEventUseCase(event)
    }
  }

  fun importShiftsFromCsv(content: String) {
    viewModelScope.launch {
      importShiftsFromCsvUseCase(currentHouseholdProvider.getCurrentHouseholdId(), content)
    }
  }
}
