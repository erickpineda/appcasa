package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.family.domain.usecase.AddMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.appcasa.core.domain.repository.FamilyRepository

@HiltViewModel
class AddMemberViewModel @Inject constructor(
  private val addMemberUseCase: AddMemberUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
  private val familyRepository: FamilyRepository
) : ViewModel() {

  private val householdId: String get() = currentHouseholdProvider.getCurrentHouseholdId()

  private val _existingNames = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(emptyList())
  val existingNames: kotlinx.coroutines.flow.StateFlow<List<String>> = _existingNames

  init {
      viewModelScope.launch {
          if (householdId.isNotEmpty()) {
              familyRepository.getMembersByHogar(householdId).collect { members ->
                  _existingNames.value = members.map { it.nombre }
              }
          }
      }
  }

  fun addMember(
    nombre: String, 
    tipo: TipoMiembro, 
    raza: String? = null, 
    color: String? = null,
    chip: String? = null, 
    avatarUrl: String? = null,
    fechaNacimiento: Long? = null
  ) {
    viewModelScope.launch {
      addMemberUseCase(
        FamilyMember(
          hogarId = householdId,
          nombre = nombre,
          tipo = tipo,
          raza = raza,
          colorPelaje = color,
          numeroChip = chip,
          avatarUrl = avatarUrl,
          fechaNacimiento = fechaNacimiento
        )
      )
    }
  }
}
