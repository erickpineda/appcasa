package com.appcasa.features.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.local.MaintenanceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeMaintenanceViewModel @Inject constructor(
    private val maintenanceDao: MaintenanceDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val householdId = currentHouseholdProvider.getCurrentHouseholdId()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<MaintenanceEntity>> = currentHouseholdProvider.householdId
        .flatMapLatest { id -> maintenanceDao.getEventsByHogar(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(
        title: String,
        cat: String,
        desc: String?,
        date: Long,
        nextDate: Long?,
        cost: Double?
    ) {
        viewModelScope.launch {
            val event = MaintenanceEntity(
                hogarId = householdId,
                titulo = title,
                categoria = cat,
                descripcion = desc,
                fechaRealizacion = date,
                proximaRevision = nextDate,
                coste = cost
            )
            val id = maintenanceDao.insertEvent(event)

            // Programar recordatorio si hay próxima revisión
            nextDate?.let { revision ->
                val remindTime = revision - (7L * 24 * 60 * 60 * 1000) // 1 semana antes
                if (remindTime > System.currentTimeMillis()) {
                    reminderScheduler.scheduleReminder(
                        id = (id + 40000).toInt(),
                        title = "Mantenimiento: $title",
                        message = "Tienes una revisión pendiente de $title la próxima semana.",
                        timeInMillis = remindTime
                    )
                }
            }
        }
    }

    fun deleteEvent(event: MaintenanceEntity) {
        viewModelScope.launch {
            maintenanceDao.deleteEvent(event)
            reminderScheduler.cancelReminder((event.id + 40000).toInt())
        }
    }
}
