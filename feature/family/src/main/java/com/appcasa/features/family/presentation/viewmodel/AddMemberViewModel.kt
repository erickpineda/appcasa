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

@HiltViewModel
class AddMemberViewModel @Inject constructor(
  private val addMemberUseCase: AddMemberUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  fun addMember(
    nombre: String, 
    tipo: TipoMiembro, 
    raza: String? = null, 
    color: String? = null,
    chip: String? = null, 
    fotoUri: String? = null,
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
          fotoUri = fotoUri,
          fechaNacimiento = fechaNacimiento
        )
      )
    }
  }
}
