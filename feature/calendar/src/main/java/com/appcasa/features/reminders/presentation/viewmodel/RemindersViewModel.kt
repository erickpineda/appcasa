package com.appcasa.features.reminders.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Reminder
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.reminders.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
  private val getRemindersUseCase: GetRemindersUseCase,
  private val addReminderUseCase: AddReminderUseCase,
  private val updateReminderUseCase: UpdateReminderUseCase,
  private val toggleReminderActiveUseCase: ToggleReminderActiveUseCase,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val reminders: StateFlow<List<Reminder>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getRemindersUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun addReminder(title: String, message: String, dateTime: Long, anticipacionMins: Int = 0) {
    viewModelScope.launch {
      addReminderUseCase(householdId, title, message, dateTime, anticipacionMins)
    }
  }

  fun updateReminder(reminder: Reminder, anticipacionMins: Int = 0) {
    viewModelScope.launch {
      updateReminderUseCase(reminder, anticipacionMins)
    }
  }

  fun toggleReminderActive(reminder: Reminder) {
    viewModelScope.launch {
      toggleReminderActiveUseCase(reminder)
    }
  }

  fun deleteReminder(reminder: Reminder) {
    viewModelScope.launch {
      deleteReminderUseCase(reminder)
    }
  }
}
