package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.UpdateConfigurationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
  private val getConfigurationUseCase: GetConfigurationUseCase,
  private val updateConfigurationUseCase: UpdateConfigurationUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val vehicleData: StateFlow<Map<String, String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getConfigurationUseCase(id) }
    .map { list -> list.associate { it.clave to it.valor } }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyMap()
    )

  fun saveVehicleData(plate: String, insurance: String, insurancePhone: String, model: String, year: String) {
    viewModelScope.launch {
      updateConfigurationUseCase(householdId, "VEH_PLATE", plate)
      updateConfigurationUseCase(householdId, "VEH_INSURANCE", insurance)
      updateConfigurationUseCase(householdId, "VEH_INSURANCE_PHONE", insurancePhone)
      updateConfigurationUseCase(householdId, "VEH_MODEL", model)
      updateConfigurationUseCase(householdId, "VEH_YEAR", year)
    }
  }
}
