package com.appcasa.features.pets.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.usecase.household.GetMemberByIdUseCase
import com.appcasa.features.pets.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetDetailViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getMemberByIdUseCase: GetMemberByIdUseCase,
  private val getPetWeightsUseCase: GetPetWeightsUseCase,
  private val addPetWeightUseCase: AddPetWeightUseCase,
  private val deletePetWeightUseCase: DeletePetWeightUseCase,
  private val getPetVaccinesUseCase: GetPetVaccinesUseCase,
  private val addPetVaccineUseCase: AddPetVaccineUseCase,
  private val deletePetVaccineUseCase: DeletePetVaccineUseCase,
  private val getPetMedicationsUseCase: GetPetMedicationsUseCase,
  private val addPetMedicationUseCase: AddPetMedicationUseCase,
  private val updatePetMedicationUseCase: UpdatePetMedicationUseCase,
  private val deletePetMedicationUseCase: DeletePetMedicationUseCase,
  private val getPetDewormingsUseCase: GetPetDewormingsUseCase,
  private val addPetDewormingUseCase: AddPetDewormingUseCase,
  private val deletePetDewormingUseCase: DeletePetDewormingUseCase
) : ViewModel() {

  private val petId: String = checkNotNull(savedStateHandle["petId"])

  val pet: StateFlow<FamilyMember?> = viewModelScope.let {
    kotlinx.coroutines.flow.flow {
      emit(getMemberByIdUseCase(petId))
    }.stateIn(it, SharingStarted.WhileSubscribed(5000), null)
  }

  val pesos: StateFlow<List<PetWeight>> = getPetWeightsUseCase(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val vacunas: StateFlow<List<PetVaccine>> = getPetVaccinesUseCase(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val medicaciones: StateFlow<List<PetMedication>> = getPetMedicationsUseCase(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val desparasitaciones: StateFlow<List<PetDeworming>> = getPetDewormingsUseCase(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  fun addPeso(valor: Double) {
    viewModelScope.launch {
      addPetWeightUseCase(petId, valor)
    }
  }

  fun deletePeso(peso: PetWeight) {
    viewModelScope.launch {
      deletePetWeightUseCase(peso)
    }
  }

  fun addMedicacion(nombre: String, dosis: String, frecuencia: String) {
    viewModelScope.launch {
      addPetMedicationUseCase(petId, nombre, dosis, frecuencia)
    }
  }

  fun updateMedicacion(med: PetMedication) {
    viewModelScope.launch {
      updatePetMedicationUseCase(med)
    }
  }

  fun deleteMedicacion(med: PetMedication) {
    viewModelScope.launch {
      deletePetMedicationUseCase(med)
    }
  }

  fun addVacuna(nombre: String) {
    viewModelScope.launch {
      addPetVaccineUseCase(petId, nombre)
    }
  }

  fun deleteVacuna(vacuna: PetVaccine) {
    viewModelScope.launch {
      deletePetVaccineUseCase(vacuna)
    }
  }

  fun addDesparasitacion(tipo: String, producto: String) {
    viewModelScope.launch {
      addPetDewormingUseCase(petId, tipo, producto)
    }
  }

  fun deleteDesparasitacion(item: PetDeworming) {
    viewModelScope.launch {
      deletePetDewormingUseCase(item)
    }
  }
}
