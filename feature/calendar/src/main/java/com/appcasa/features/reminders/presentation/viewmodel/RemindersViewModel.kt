package com.appcasa.features.reminders.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.data.local.RecordatorioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineDispatcher
import com.appcasa.core.domain.di.IoDispatcher

@HiltViewModel
class RemindersViewModel @Inject constructor(
  private val recordatorioDao: RecordatorioDao,
  private val configuracionDao: ConfiguracionDao,
  private val reminderScheduler: ReminderScheduler,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  val reminders: StateFlow<List<RecordatorioEntity>> = recordatorioDao.getRecordatoriosByHogar(householdId)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun addReminder(title: String, message: String, dateTime: Long, anticipacionMins: Int = 0) {
    viewModelScope.launch(ioDispatcher) {
      val reminder = RecordatorioEntity(
        hogarId = householdId,
        titulo = title,
        descripcion = message,
        fechaHora = dateTime,
        activo = true
      )
      val id = recordatorioDao.insertRecordatorio(reminder)
      
      // Programamos con anticipación
      val scheduledTime = dateTime - (anticipacionMins * 60 * 1000)
      reminderScheduler.scheduleReminder(
        id = id.toInt(),
        title = title,
        message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins minutos: $message" else message,
        timeInMillis = scheduledTime
      )
    }
  }

  fun updateReminder(reminder: RecordatorioEntity, anticipacionMins: Int = 0) {
    viewModelScope.launch(ioDispatcher) {
      recordatorioDao.updateRecordatorio(reminder)
      if (reminder.activo) {
        val scheduledTime = reminder.fechaHora - (anticipacionMins * 60 * 1000)
        reminderScheduler.scheduleReminder(
          id = reminder.id.toInt(),
          title = reminder.titulo,
          message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins min: ${reminder.descripcion ?: ""}" else (reminder.descripcion ?: ""),
          timeInMillis = scheduledTime
        )
      } else {
        reminderScheduler.cancelReminder(reminder.id.toInt())
      }
    }
  }

  fun toggleReminderActive(reminder: RecordatorioEntity) {
    viewModelScope.launch(ioDispatcher) {
      val updated = reminder.copy(activo = !reminder.activo, updatedAt = System.currentTimeMillis())
      recordatorioDao.updateRecordatorio(updated)
      
      if (updated.activo) {
        reminderScheduler.scheduleReminder(
          id = updated.id.toInt(),
          title = updated.titulo,
          message = updated.descripcion ?: "",
          timeInMillis = updated.fechaHora
        )
      } else {
        reminderScheduler.cancelReminder(updated.id.toInt())
      }
    }
  }

  fun deleteReminder(reminder: RecordatorioEntity) {
    viewModelScope.launch {
      recordatorioDao.deleteRecordatorio(reminder)
      reminderScheduler.cancelReminder(reminder.id.toInt())
    }
  }
}
