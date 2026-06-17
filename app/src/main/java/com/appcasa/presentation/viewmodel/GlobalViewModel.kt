package com.appcasa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.config.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.user.GetCurrentUserUseCase
import com.appcasa.features.settings.domain.usecase.GetCurrentHouseholdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.appcasa.core.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
  private val getConfigurationUseCase: GetConfigurationUseCase,
  private val getCurrentUserUseCase: GetCurrentUserUseCase,
  private val getCurrentHouseholdUseCase: GetCurrentHouseholdUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
  private val settingsRepository: SettingsRepository
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
        // Un hogar está configurado si existe en Room Y el usuario persistido también existe.
        // Si usuario.id es -1L, es un usuario de Firebase sin hogar local aún.
        hogar != null && usuario != null && usuario.id != -1L
    }
    .stateIn(
        scope = viewModelScope, 
        started = SharingStarted.WhileSubscribed(5000), 
        initialValue = if (currentHouseholdProvider.getCurrentHouseholdId() != 0L) true else null
    )

  val currentUser = getCurrentUserUseCase()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _isSecureMode = MutableStateFlow(false)
  val isSecureMode = _isSecureMode.asStateFlow()

  fun setSecureMode(enabled: Boolean) {
      _isSecureMode.value = enabled
  }

  fun triggerManualSync() {
      val id = currentHouseholdProvider.getCurrentHouseholdId()
      if (id != 0L) {
          viewModelScope.launch {
              settingsRepository.triggerManualSync(id)
          }
      }
  }
}
