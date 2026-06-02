package com.appcasa.features.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.maintenance.data.local.MaintenanceDao
import com.appcasa.features.maintenance.data.local.MaintenanceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _activePage = MutableStateFlow(1)
    val activePage = _activePage.asStateFlow()

    private val _archivedPage = MutableStateFlow(1)
    val archivedPage = _archivedPage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(replay = 0)
    val toastEvent = _toastEvent.asSharedFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<MaintenanceEntity>> = combine(
        currentHouseholdProvider.householdId,
        _activePage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            maintenanceDao.getEventsPaged(id, limit = page * 20, offset = 0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val archivedEvents: StateFlow<List<MaintenanceEntity>> = combine(
        currentHouseholdProvider.householdId,
        _archivedPage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            maintenanceDao.getArchivedEventsPaged(id, limit = page * 20, offset = 0)
        }
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

    fun archiveEvent(event: MaintenanceEntity) {
        viewModelScope.launch {
            maintenanceDao.insertEvent(event.copy(archived = true))
            reminderScheduler.cancelReminder((event.id + 40000).toInt())
        }
    }

    fun unarchiveEvent(eventId: Long) {
        viewModelScope.launch {
            maintenanceDao.unarchiveEvent(eventId)
        }
    }

    fun loadMoreActive() {
        if (_isLoading.value) return
        val currentCount = events.value.size
        _activePage.value += 1
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600)
            if (events.value.size <= currentCount) {
                _toastEvent.emit("No hay más registros para cargar")
                _activePage.value -= 1
            }
            _isLoading.value = false
        }
    }

    fun loadMoreArchived() {
        if (_isLoading.value) return
        val currentCount = archivedEvents.value.size
        _archivedPage.value += 1
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600)
            if (archivedEvents.value.size <= currentCount) {
                _toastEvent.emit("No hay más registros en el archivo")
                _archivedPage.value -= 1
            }
            _isLoading.value = false
        }
    }

    fun clearAllArchived() {
        viewModelScope.launch {
            maintenanceDao.deleteAllArchivedMaintenanceEvents(householdId)
        }
    }

    fun archiveOldEvents() {
        viewModelScope.launch {
            val threshold = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
            maintenanceDao.archiveOldMaintenanceEvents(householdId, threshold)
        }
    }
}
