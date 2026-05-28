package com.appcasa.features.utilities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.utilities.data.local.UtilidadDao
import com.appcasa.features.utilities.data.local.UtilidadEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UtilitiesViewModel @Inject constructor(
    private val utilidadDao: UtilidadDao
) : ViewModel() {

    val utilities: StateFlow<List<UtilidadEntity>> = utilidadDao.getUtilidades()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun initializeUtilities() {
        viewModelScope.launch {
            val current = utilities.value
            if (current.isEmpty()) {
                val initial = listOf(
                    UtilidadEntity(codigo = "CALC_DOSIS", nombre = "Dosis Mascotas", descripcion = "Cálculo según peso", icono = "medication", orden = 1),
                    UtilidadEntity(codigo = "CALC_IMC", nombre = "IMC Familiar", descripcion = "Índice de Masa Corporal", icono = "monitor_weight", orden = 2),
                    UtilidadEntity(codigo = "CALC_HIPOTECA", nombre = "Hipoteca", descripcion = "Cuota mensual e intereses", icono = "home", orden = 3),
                    UtilidadEntity(codigo = "CALC_EDAD", nombre = "Edad Exacta", descripcion = "Años, meses y días", icono = "cake", orden = 4),
                    UtilidadEntity(codigo = "CALC_CONSUMO", nombre = "Consumo Eléctrico", descripcion = "Estimación mensual", icono = "bolt", orden = 5),
                    UtilidadEntity(codigo = "CALC_AHORRO", nombre = "Ahorro Mensual", descripcion = "Objetivo de ahorro", icono = "savings", orden = 6)
                )
                initial.forEach { utilidadDao.insertUtilidad(it) }
            }
        }
    }
}
