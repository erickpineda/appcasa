package com.appcasa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
  configuracionDao: ConfiguracionDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  private val configs = configuracionDao.getConfiguracion(householdId)
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

  val isHouseholdSetup: StateFlow<Boolean?> = configuracionDao.getHogarActual()
    .combine(configuracionDao.getUsuarioActual()) { hogar, usuario ->
        hogar != null && usuario != null
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val currentUser = configuracionDao.getUsuarioActual()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
