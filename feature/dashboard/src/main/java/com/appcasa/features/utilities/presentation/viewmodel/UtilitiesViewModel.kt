package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.utilities.data.local.UtilidadDao
import com.appcasa.features.utilities.data.local.UtilidadEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.ConfiguracionEntity
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UtilitiesViewModel @Inject constructor(
  private val utilidadDao: UtilidadDao,
  private val configuracionDao: ConfiguracionDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  val utilities: StateFlow<List<UtilidadEntity>> = utilidadDao.getUtilidades()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val savedValues: StateFlow<Map<String, String>> = configuracionDao.getConfiguracion(householdId)
    .map { list -> list.associate { it.clave to it.valor } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

  init {
    viewModelScope.launch {
        // Aseguramos que todas las utilidades estén registradas, incluso si faltan algunas nuevas
        initializeUtilities()
    }
  }

  fun saveValue(clave: String, valor: String) {
    viewModelScope.launch {
      configuracionDao.insertConfiguracion(ConfiguracionEntity(hogarId = householdId, clave = clave, valor = valor))
    }
  }

  fun initializeUtilities() {
    viewModelScope.launch {
        val currentCodes = utilities.value.map { it.codigo }.toSet()
        val allPossible = listOf(
          UtilidadEntity(codigo = "CALC_DOSIS", nombre = "Dosis Mascotas", descripcion = "Cálculo según peso", icono = "medication", orden = 1, categoria = "Salud"),
          UtilidadEntity(codigo = "CALC_IMC", nombre = "IMC Familiar", descripcion = "Índice de Masa Corporal", icono = "monitor_weight", orden = 2, categoria = "Salud"),
          UtilidadEntity(codigo = "CALC_HIPOTECA", nombre = "Hipoteca", descripcion = "Cuota mensual e intereses", icono = "home", orden = 3, categoria = "Finanzas"),
          UtilidadEntity(codigo = "FIN_GASTOS", nombre = "Gastos", descripcion = "Control de presupuesto", icono = "payments", orden = 4, categoria = "Finanzas"),
          UtilidadEntity(codigo = "CALC_AHORRO", nombre = "Ahorro Mensual", descripcion = "Objetivo de ahorro", icono = "savings", orden = 5, categoria = "Finanzas"),
          UtilidadEntity(codigo = "CALC_EDAD", nombre = "Edad Exacta", descripcion = "Años, meses y días", icono = "cake", orden = 6, categoria = "Varios"),
          UtilidadEntity(codigo = "CALC_CONSUMO", nombre = "Consumo Eléctrico", descripcion = "Estimación mensual", icono = "bolt", orden = 7, categoria = "Varios"),
          UtilidadEntity(codigo = "VEH_MGR", nombre = "Mi Vehículo", descripcion = "Seguro y mantenimiento", icono = "directions_car", orden = 8, categoria = "Varios"),
          UtilidadEntity(codigo = "UTIL_PDF", nombre = "Fotos a PDF", descripcion = "Convertir imágenes a PDF", icono = "picture_as_pdf", orden = 9, categoria = "Productividad"),
          UtilidadEntity(codigo = "UTIL_WIFI", nombre = "QR WiFi", descripcion = "Compartir clave WiFi", icono = "qr_code", orden = 10, categoria = "Productividad"),
          UtilidadEntity(codigo = "UTIL_COCINA", nombre = "Cocina", descripcion = "Conversor de medidas", icono = "restaurant", orden = 11, categoria = "Varios"),
          UtilidadEntity(codigo = "UTIL_PIENSO", nombre = "Ración Pienso", descripcion = "Guía de alimentación", icono = "pets", orden = 12, categoria = "Salud"),
          UtilidadEntity(codigo = "UTIL_SAFE", nombre = "Smart Safe", descripcion = "Documentos y garantías", icono = "lock", orden = 13, categoria = "Productividad")
        )
        
        allPossible.forEach { utility ->
          if (!currentCodes.contains(utility.codigo)) {
            utilidadDao.insertUtilidad(utility)
          }
        }
    }
  }
}
