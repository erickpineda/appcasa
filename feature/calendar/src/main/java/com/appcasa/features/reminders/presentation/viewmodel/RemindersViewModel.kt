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
import kotlinx.coroutines.flow.first
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineDispatcher
import com.appcasa.core.domain.di.IoDispatcher

@HiltViewModel
class RemindersViewModel @Inject constructor(
  private val recordatorioDao: RecordatorioDao,
  private val configuracionDao: ConfiguracionDao,
  private val reminderScheduler: ReminderScheduler,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

  val reminders: StateFlow<List<RecordatorioEntity>> = recordatorioDao.getRecordatoriosByHogar(1L)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun addReminder(title: String, message: String, dateTime: Long) {
    viewModelScope.launch(ioDispatcher) {
      val hogarId = configuracionDao.getHogarActual().first()?.id ?: 1L
      val reminder = RecordatorioEntity(
        hogarId = hogarId,
        titulo = title,
        descripcion = message,
        fechaHora = dateTime,
        activo = true
      )
      val id = recordatorioDao.insertRecordatorio(reminder)
      reminderScheduler.scheduleReminder(
        id = id.toInt(),
        title = title,
        message = message,
        timeInMillis = dateTime
      )
    }
  }

  fun updateReminder(reminder: RecordatorioEntity) {
    viewModelScope.launch(ioDispatcher) {
      recordatorioDao.updateRecordatorio(reminder)
      if (reminder.activo) {
        reminderScheduler.scheduleReminder(
          id = reminder.id.toInt(),
          title = reminder.titulo,
          message = reminder.descripcion ?: "",
          timeInMillis = reminder.fechaHora
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
