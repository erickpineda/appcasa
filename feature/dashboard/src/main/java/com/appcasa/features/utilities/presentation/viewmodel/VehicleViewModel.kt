package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.ConfiguracionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
  private val configuracionDao: ConfiguracionDao
) : ViewModel() {

  // Usamos la tabla de configuración para guardar datos del vehículo de forma persistente
  val vehicleData: StateFlow<Map<String, String>> = configuracionDao.getConfiguracion(1L)
    .map { list -> list.associate { it.clave to it.valor } }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyMap()
    )

  fun saveVehicleData(plate: String, insurance: String, insurancePhone: String, model: String, year: String) {
    viewModelScope.launch {
      configuracionDao.insertConfiguracion(ConfiguracionEntity(hogarId = 1L, clave = "VEH_PLATE", valor = plate))
      configuracionDao.insertConfiguracion(ConfiguracionEntity(hogarId = 1L, clave = "VEH_INSURANCE", valor = insurance))
      configuracionDao.insertConfiguracion(ConfiguracionEntity(hogarId = 1L, clave = "VEH_INSURANCE_PHONE", valor = insurancePhone))
      configuracionDao.insertConfiguracion(ConfiguracionEntity(hogarId = 1L, clave = "VEH_MODEL", valor = model))
      configuracionDao.insertConfiguracion(ConfiguracionEntity(hogarId = 1L, clave = "VEH_YEAR", valor = year))
    }
  }
}
