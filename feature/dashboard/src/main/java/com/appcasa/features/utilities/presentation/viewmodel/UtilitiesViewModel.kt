package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Utility
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.repository.ConfigurationRepository
import com.appcasa.features.utilities.domain.usecase.GetUtilitiesUseCase
import com.appcasa.features.utilities.domain.usecase.InitializeUtilitiesUseCase
import com.appcasa.features.utilities.domain.usecase.SaveUtilityValueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UtilitiesViewModel @Inject constructor(
  private val initializeUtilitiesUseCase: InitializeUtilitiesUseCase,
  private val saveUtilityValueUseCase: SaveUtilityValueUseCase,
  private val getUtilitiesUseCase: GetUtilitiesUseCase,
  private val configurationRepository: ConfigurationRepository,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: String get() = currentHouseholdProvider.getCurrentHouseholdId()

  val utilities: StateFlow<List<Utility>> = getUtilitiesUseCase()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val savedValues: StateFlow<Map<String, String>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> configurationRepository.getConfiguracion(id) }
    .map { list -> list.associate { it.clave to it.valor } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  init {
    viewModelScope.launch {
        initializeUtilities()
    }
  }

  fun saveValue(clave: String, valor: String) {
    viewModelScope.launch {
      saveUtilityValueUseCase(householdId, clave, valor)
    }
  }

  fun initializeUtilities() {
    viewModelScope.launch {
        initializeUtilitiesUseCase(utilities.value)
    }
  }
}
