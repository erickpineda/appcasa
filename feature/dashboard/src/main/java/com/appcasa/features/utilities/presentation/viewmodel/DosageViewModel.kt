package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.pets.domain.usecase.GetPetWeightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DosageViewModel @Inject constructor(
    private val getPetWeightsUseCase: GetPetWeightsUseCase
) : ViewModel() {

    private val _petWeight = MutableStateFlow(0.0)
    val petWeight: StateFlow<Double> = _petWeight.asStateFlow()

    fun updateWeightForPet(petId: Long) {
        viewModelScope.launch {
            val weights = getPetWeightsUseCase(petId).first()
            _petWeight.value = weights.lastOrNull()?.pesoKg ?: 0.0
        }
    }

    fun setManualWeight(weight: Double) {
        _petWeight.value = weight
    }
}
