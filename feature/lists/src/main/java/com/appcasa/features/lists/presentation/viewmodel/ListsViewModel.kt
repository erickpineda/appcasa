package com.appcasa.features.lists.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Lista
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.lists.domain.usecase.*
import com.appcasa.core.domain.usecase.lists.GetActiveListsUseCase
import com.appcasa.core.domain.usecase.config.IsCompactViewUseCase
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
class ListsViewModel @Inject constructor(
    private val getActiveListsUseCase: GetActiveListsUseCase,
    private val getArchivedListsUseCase: GetArchivedListsUseCase,
    private val createListUseCase: CreateListUseCase,
    private val updateListUseCase: UpdateListUseCase,
    private val deleteListUseCase: DeleteListUseCase,
    private val archiveListUseCase: ArchiveListUseCase,
    private val unarchiveListUseCase: UnarchiveListUseCase,
    private val clearArchivedListsUseCase: ClearArchivedListsUseCase,
    private val purgeCompletedItemsUseCase: PurgeCompletedItemsUseCase,
    private val isCompactViewUseCase: IsCompactViewUseCase,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val householdId: String get() = currentHouseholdProvider.getCurrentHouseholdId()

    private val _activePage = MutableStateFlow(1)
    val activePage = _activePage.asStateFlow()

    private val _archivedPage = MutableStateFlow(1)
    val archivedPage = _archivedPage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(replay = 0)
    val toastEvent = _toastEvent.asSharedFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val lists: StateFlow<List<Lista>> = combine(
        currentHouseholdProvider.householdId,
        _activePage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            getActiveListsUseCase(id, page)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val archivedLists: StateFlow<List<Lista>> = combine(
        currentHouseholdProvider.householdId,
        _archivedPage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            getArchivedListsUseCase(id, page)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Lista>())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isCompactView: StateFlow<Boolean> = currentHouseholdProvider.householdId
        .flatMapLatest { id -> isCompactViewUseCase(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addList(nombre: String, tipo: String) {
        viewModelScope.launch {
            createListUseCase(householdId, nombre, tipo)
        }
    }

    fun deleteList(lista: Lista) {
        viewModelScope.launch {
            deleteListUseCase(lista)
        }
    }

    fun updateList(lista: Lista, nuevoNombre: String) {
        viewModelScope.launch {
            updateListUseCase(lista, nuevoNombre)
        }
    }

    fun archiveList(lista: Lista) {
        viewModelScope.launch {
            archiveListUseCase(lista)
        }
    }

    fun unarchiveList(listaId: String) {
        viewModelScope.launch {
            unarchiveListUseCase(listaId)
        }
    }

    fun loadMoreActive() {
        if (_isLoading.value) return
        val currentCount = lists.value.size
        _activePage.value += 1
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600)
            if (lists.value.size <= currentCount) {
                _toastEvent.emit("No hay más listas para cargar")
                _activePage.value -= 1
            }
            _isLoading.value = false
        }
    }

    fun loadMoreArchived() {
        if (_isLoading.value) return
        val currentCount = archivedLists.value.size
        _archivedPage.value += 1
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(600)
            if (archivedLists.value.size <= currentCount) {
                _toastEvent.emit("No hay más registros en el archivo")
                _archivedPage.value -= 1
            }
            _isLoading.value = false
        }
    }

    fun clearAllArchived() {
        viewModelScope.launch {
            clearArchivedListsUseCase(householdId)
        }
    }

    fun purgeCompletedItems(listaId: String) {
        viewModelScope.launch {
            purgeCompletedItemsUseCase(listaId)
        }
    }
}
