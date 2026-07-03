package com.appcasa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.config.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.user.GetCurrentUserUseCase
import com.appcasa.features.settings.domain.usecase.GetCurrentHouseholdUseCase
import com.appcasa.features.settings.domain.usecase.ForceSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
  private val getConfigurationUseCase: GetConfigurationUseCase,
  private val getCurrentUserUseCase: GetCurrentUserUseCase,
  private val getCurrentHouseholdUseCase: GetCurrentHouseholdUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
  private val forceSyncUseCase: ForceSyncUseCase
) : ViewModel() {

  private val householdId: String get() = currentHouseholdProvider.getCurrentHouseholdId()

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
      // Un hogar está configurado si existe en Room local.
      // Ignoramos el estado del usuario de Firebase temporalmente para que la app no se bloquee
      // si Google Play Services falla al devolver el perfil.
      val hasHouse = hogar != null && hogar.id.isNotEmpty()
      val hasUser = usuario != null && usuario.id.isNotEmpty() && usuario.id != "volatile_id"
      
      if (hasHouse && hasUser) true 
      else if (hasHouse && !hasUser) false // Necesita elegir perfil
      else false // No hay casa
    }
    .stateIn(
      scope = viewModelScope, 
      started = SharingStarted.WhileSubscribed(5000), 
      initialValue = null
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
    if (id.isNotEmpty()) {
      viewModelScope.launch {
        forceSyncUseCase(id)
      }
    }
  }
}
