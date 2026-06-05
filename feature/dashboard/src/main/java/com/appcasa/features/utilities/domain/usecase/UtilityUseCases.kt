package com.appcasa.features.utilities.domain.usecase

import com.appcasa.core.domain.model.Configuration
import com.appcasa.core.domain.model.Utility
import com.appcasa.core.domain.repository.ConfigurationRepository
import com.appcasa.core.domain.repository.UtilityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUtilitiesUseCase @Inject constructor(
    private val repository: UtilityRepository
) {
    operator fun invoke(): Flow<List<Utility>> {
        return repository.getUtilidades()
    }
}

class SaveUtilityValueUseCase @Inject constructor(
    private val repository: ConfigurationRepository
) {
    suspend operator fun invoke(hogarId: Long, clave: String, valor: String) {
        repository.insertConfiguracion(Configuration(hogarId = hogarId, clave = clave, valor = valor))
    }
}

class InitializeUtilitiesUseCase @Inject constructor(
    private val repository: UtilityRepository
) {
    suspend operator fun invoke(currentUtilities: List<Utility>) {
        val currentCodes = currentUtilities.map { it.codigo }.toSet()
        val allPossible = listOf(
          Utility(codigo = "CALC_DOSIS", nombre = "Dosis Mascotas", descripcion = "Cálculo según peso", icono = "medication", orden = 1, categoria = "Salud"),
          Utility(codigo = "CALC_IMC", nombre = "IMC Familiar", descripcion = "Índice de Masa Corporal", icono = "monitor_weight", orden = 2, categoria = "Salud"),
          Utility(codigo = "CALC_HIPOTECA", nombre = "Hipoteca", descripcion = "Cuota mensual e intereses", icono = "home", orden = 3, categoria = "Finanzas"),
          Utility(codigo = "FIN_GASTOS", nombre = "Gastos", descripcion = "Control de presupuesto", icono = "payments", orden = 4, categoria = "Finanzas"),
          Utility(codigo = "CALC_AHORRO", nombre = "Ahorro Mensual", descripcion = "Objetivo de ahorro", icono = "savings", orden = 5, categoria = "Finanzas"),
          Utility(codigo = "CALC_EDAD", nombre = "Edad Exacta", descripcion = "Años, meses y días", icono = "cake", orden = 6, categoria = "Varios"),
          Utility(codigo = "CALC_CONSUMO", nombre = "Consumo Eléctrico", descripcion = "Estimación mensual", icono = "bolt", orden = 7, categoria = "Varios"),
          Utility(codigo = "VEH_MGR", nombre = "Mi Vehículo", descripcion = "Seguro y mantenimiento", icono = "directions_car", orden = 8, categoria = "Varios"),
          Utility(codigo = "UTIL_PDF", nombre = "Fotos a PDF", descripcion = "Convertir imágenes a PDF", icono = "picture_as_pdf", orden = 9, categoria = "Productividad"),
          Utility(codigo = "UTIL_WIFI", nombre = "QR WiFi", descripcion = "Compartir clave WiFi", icono = "qr_code", orden = 10, categoria = "Productividad"),
          Utility(codigo = "UTIL_COCINA", nombre = "Cocina", descripcion = "Conversor de medidas", icono = "restaurant", orden = 11, categoria = "Varios"),
          Utility(codigo = "UTIL_PIENSO", nombre = "Ración Pienso", descripcion = "Guía de alimentación", icono = "pets", orden = 12, categoria = "Salud"),
          Utility(codigo = "UTIL_SAFE", nombre = "Smart Safe", descripcion = "Documentos y garantías", icono = "lock", orden = 13, categoria = "Productividad")
        )
        
        allPossible.forEach { utility ->
          if (!currentCodes.contains(utility.codigo)) {
            repository.insertUtilidad(utility)
          }
        }
    }
}
