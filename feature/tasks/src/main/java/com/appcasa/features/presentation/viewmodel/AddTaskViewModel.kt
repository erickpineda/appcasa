package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Prioridad
import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val tareaDao: TareaDao,
    private val miembroDao: MiembroDao,
    private val configuracionDao: ConfiguracionDao
) : ViewModel() {

    val familyMembers: StateFlow<List<MiembroEntity>> = miembroDao.getMiembrosByHogar(1L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(titulo: String, prioridad: Prioridad, asignadoId: Long? = null, esPersonal: Boolean = false, fotoUri: String? = null) {
        viewModelScope.launch {
            val hogarId = configuracionDao.getHogarActual().first()?.id ?: 1L
            val tareaId = tareaDao.insertTarea(
                TareaEntity(
                    hogarId = hogarId,
                    titulo = titulo,
                    prioridad = prioridad.name,
                    esPersonal = esPersonal,
                    fotoUri = fotoUri
                )
            )
//... (el resto igual) ...
            
            if (asignadoId != null) {
                tareaDao.insertAsignacion(
                    com.appcasa.features.tasks.data.local.TareaAsignacionEntity(
                        tareaId = tareaId,
                        miembroId = asignadoId
                    )
                )
            }
        }
    }
}
