package com.appcasa.features.lists.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val listaDao: ListaDao,
    private val configuracionDao: ConfiguracionDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

    private val _activePage = MutableStateFlow(1)
    val activePage = _activePage.asStateFlow()

    private val _archivedPage = MutableStateFlow(1)
    val archivedPage = _archivedPage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(replay = 0)
    val toastEvent = _toastEvent.asSharedFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val lists: StateFlow<List<ListaEntity>> = combine(
        currentHouseholdProvider.householdId,
        _activePage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            listaDao.getListasPaged(id, limit = page * 20, offset = 0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val archivedLists: StateFlow<List<ListaEntity>> = combine(
        currentHouseholdProvider.householdId,
        _archivedPage
    ) { id, page -> id to page }
        .flatMapLatest { (id, page) -> 
            listaDao.getArchivedListasPaged(id, limit = page * 20, offset = 0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isCompactView: StateFlow<Boolean> = configuracionDao.getConfiguracion(householdId)
        .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addList(nombre: String, tipo: String) {
        viewModelScope.launch {
            listaDao.insertLista(
                ListaEntity(
                    hogarId = householdId,
                    nombre = nombre,
                    tipo = tipo
                )
            )
        }
    }

    fun deleteList(lista: ListaEntity) {
        viewModelScope.launch {
            listaDao.deleteLista(lista)
        }
    }

    fun updateList(lista: ListaEntity, nuevoNombre: String) {
        viewModelScope.launch {
            listaDao.insertLista(lista.copy(nombre = nuevoNombre))
        }
    }

    fun archiveList(lista: ListaEntity) {
        viewModelScope.launch {
            listaDao.insertLista(lista.copy(archived = true))
        }
    }

    fun unarchiveList(listaId: Long) {
        viewModelScope.launch {
            listaDao.unarchiveLista(listaId)
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
            listaDao.deleteAllArchivedListas(householdId)
        }
    }

    fun purgeCompletedItems(listaId: Long) {
        viewModelScope.launch {
            listaDao.deleteCompletedItems(listaId)
        }
    }
}
