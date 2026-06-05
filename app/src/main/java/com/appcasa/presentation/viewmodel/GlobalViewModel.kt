package com.appcasa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.GetCurrentUserUseCase
import com.appcasa.features.settings.domain.usecase.GetCurrentHouseholdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
  private val getConfigurationUseCase: GetConfigurationUseCase,
  private val getCurrentUserUseCase: GetCurrentUserUseCase,
  private val getCurrentHouseholdUseCase: GetCurrentHouseholdUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  private val configs = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getConfigurationUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val isDarkMode: StateFlow<Boolean> = configs.map { list ->
    list.find { it.clave == "tema_oscuro" }?.valor == "true"
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val isCompactView: StateFlow<Boolean> = configs.map { list ->
    list.find { it.clave == "vista_compacta" }?.valor == "true"
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val isShopMode: StateFlow<Boolean> = configs.map { list ->
    list.find { it.clave == "modo_tienda" }?.valor == "true"
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val partnerNotifsEnabled: StateFlow<Boolean> = configs.map { list ->
    list.find { it.clave == "notif_pareja" }?.valor != "false"
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val currencySymbol: StateFlow<String> = configs.map { list ->
    list.find { it.clave == "moneda" }?.valor ?: "€"
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "€")

  val isHouseholdSetup: StateFlow<Boolean?> = getCurrentHouseholdUseCase()
    .combine(getCurrentUserUseCase()) { hogar, usuario ->
        hogar != null && usuario != null
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val currentUser = getCurrentUserUseCase()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
