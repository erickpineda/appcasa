package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val miembroDao: MiembroDao
) : ViewModel() {

    // Obtenemos todos los miembros del hogar 1 (MVP)
    val familyMembers: StateFlow<List<MiembroEntity>> = miembroDao.getMiembrosByHogar(1L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pets: StateFlow<List<MiembroEntity>> = familyMembers.map { members ->
        members.filter { it.tipo != TipoMiembro.PERSONA.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val people: StateFlow<List<MiembroEntity>> = familyMembers.map { members ->
        members.filter { it.tipo == TipoMiembro.PERSONA.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteMember(member: MiembroEntity) {
        viewModelScope.launch {
            miembroDao.deleteMiembro(member)
        }
    }
}
