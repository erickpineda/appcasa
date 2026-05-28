package com.appcasa.features.lists.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val listaDao: ListaDao
) : ViewModel() {

    val lists: StateFlow<List<ListaEntity>> = listaDao.getListasByHogar(1L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addList(nombre: String, tipo: String) {
        viewModelScope.launch {
            listaDao.insertLista(
                ListaEntity(
                    hogarId = 1L,
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
