package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.pets.data.local.MascotaDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DosageViewModel @Inject constructor(
    private val mascotaDao: MascotaDao
) : ViewModel() {

    private val _petWeight = MutableStateFlow(0.0)
    val petWeight: StateFlow<Double> = _petWeight.asStateFlow()

    fun updateWeightForPet(petId: Long) {
        viewModelScope.launch {
            val latestPeso = mascotaDao.getLatestPeso(petId)
            _petWeight.value = latestPeso?.pesoKg ?: 0.0
        }
    }

    fun setManualWeight(weight: Double) {
        _petWeight.value = weight
    }
}
