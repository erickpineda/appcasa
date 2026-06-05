package com.appcasa.features.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.MaintenanceEvent
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.maintenance.domain.usecase.*
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
    private val getMaintenanceEventsUseCase: GetMaintenanceEventsUseCase,
    private val getArchivedMaintenanceEventsUseCase: GetArchivedMaintenanceEventsUseCase,
    private val addMaintenanceEventUseCase: AddMaintenanceEventUseCase,
    private val deleteMaintenanceEventUseCase: DeleteMaintenanceEventUseCase,
    private val archiveMaintenanceEventUseCase: ArchiveMaintenanceEventUseCase,
    private val unarchiveMaintenanceEventUseCase: UnarchiveMaintenanceEventUseCase,
    private val clearAllArchivedMaintenanceUseCase: ClearAllArchivedMaintenanceUseCase,
    private val archiveOldMaintenanceEventsUseCase: ArchiveOldMaintenanceEventsUseCase,
    private val currentHouseholdProvider: CurrentHouseholdProvider
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
    val events: StateFlow<List<MaintenanceEvent>> = combine(
        currentHouseholdProvider.householdId,
        _activePage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            getMaintenanceEventsUseCase(id, page)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val archivedEvents: StateFlow<List<MaintenanceEvent>> = combine(
        currentHouseholdProvider.householdId,
        _archivedPage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            getArchivedMaintenanceEventsUseCase(id, page)
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
            addMaintenanceEventUseCase(householdId, title, cat, desc, date, nextDate, cost)
        }
    }

    fun deleteEvent(event: MaintenanceEvent) {
        viewModelScope.launch {
            deleteMaintenanceEventUseCase(event)
        }
    }

    fun archiveEvent(event: MaintenanceEvent) {
        viewModelScope.launch {
            archiveMaintenanceEventUseCase(event)
        }
    }

    fun unarchiveEvent(eventId: Long) {
        viewModelScope.launch {
            unarchiveMaintenanceEventUseCase(eventId)
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
            clearAllArchivedMaintenanceUseCase(householdId)
        }
    }

    fun archiveOldEvents() {
        viewModelScope.launch {
            archiveOldMaintenanceEventsUseCase(householdId)
        }
    }
}
