package com.appcasa.features.lists.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
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
class ListsViewModel @Inject constructor(
    private val listaDao: ListaDao,
    private val configuracionDao: ConfiguracionDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

    val lists: StateFlow<List<ListaEntity>> = listaDao.getListasByHogar(householdId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
}
