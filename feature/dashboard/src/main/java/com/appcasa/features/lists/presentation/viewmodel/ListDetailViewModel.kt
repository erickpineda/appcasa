package com.appcasa.features.lists.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.ListaItem
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.IsCompactViewUseCase
import com.appcasa.features.lists.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val addListItemUseCase: AddListItemUseCase,
    private val toggleListItemUseCase: ToggleListItemUseCase,
    private val updateListItemUseCase: UpdateListItemUseCase,
    private val deleteListItemUseCase: DeleteListItemUseCase,
    private val bulkDeleteItemsUseCase: BulkDeleteItemsUseCase,
    private val bulkToggleItemsUseCase: BulkToggleItemsUseCase,
    private val isCompactViewUseCase: IsCompactViewUseCase,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val listId: Long = checkNotNull(savedStateHandle["listId"])

    val items: StateFlow<List<ListaItem>> = getListItemsUseCase(listId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<ListaItem>()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isCompactView: StateFlow<Boolean> = currentHouseholdProvider.householdId
        .flatMapLatest { id -> isCompactViewUseCase(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addItem(nombre: String) {
        viewModelScope.launch {
            addListItemUseCase(listId, nombre)
        }
    }

    fun toggleItemCompletion(item: ListaItem) {
        viewModelScope.launch {
            toggleListItemUseCase(item)
        }
    }

    fun updateItem(item: ListaItem, nuevoTexto: String) {
        viewModelScope.launch {
            updateListItemUseCase(item, nuevoTexto)
        }
    }

    fun deleteItem(item: ListaItem) {
        viewModelScope.launch {
            deleteListItemUseCase(item)
        }
    }

    fun deleteItems(itemsToDelete: List<ListaItem>) {
        viewModelScope.launch {
            bulkDeleteItemsUseCase(itemsToDelete)
        }
    }

    fun toggleItemsCompletion(itemsToUpdate: List<ListaItem>, completed: Boolean) {
        viewModelScope.launch {
            bulkToggleItemsUseCase(itemsToUpdate, completed)
        }
    }
}
