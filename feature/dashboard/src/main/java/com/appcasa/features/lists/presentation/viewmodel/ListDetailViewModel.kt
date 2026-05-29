package com.appcasa.features.lists.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaItemEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val listaDao: ListaDao,
    private val configuracionDao: ConfiguracionDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val listId: Long = checkNotNull(savedStateHandle["listId"])
    private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

    val items: StateFlow<List<ListaItemEntity>> = listaDao.getItemsByLista(listId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isCompactView: StateFlow<Boolean> = configuracionDao.getConfiguracion(householdId)
        .map { list -> list.find { it.clave == "vista_compacta" }?.valor == "true" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addItem(nombre: String) {
        viewModelScope.launch {
            listaDao.insertItem(
                ListaItemEntity(
                    listaId = listId,
                    texto = nombre
                )
            )
        }
    }

    fun toggleItemCompletion(item: ListaItemEntity) {
        viewModelScope.launch {
            listaDao.updateItem(item.copy(completado = !item.completado))
        }
    }

    fun updateItem(item: ListaItemEntity, nuevoTexto: String) {
        viewModelScope.launch {
            listaDao.updateItem(item.copy(texto = nuevoTexto))
        }
    }

    fun deleteItem(item: ListaItemEntity) {
        viewModelScope.launch {
            listaDao.deleteItem(item)
        }
    }

    fun deleteItems(itemsToDelete: List<ListaItemEntity>) {
        viewModelScope.launch {
            itemsToDelete.forEach { listaDao.deleteItem(it) }
        }
    }

    fun toggleItemsCompletion(itemsToUpdate: List<ListaItemEntity>, completed: Boolean) {
        viewModelScope.launch {
            itemsToUpdate.forEach {
                listaDao.updateItem(it.copy(completado = completed))
            }
        }
    }
}
