package com.appcasa.features.pets.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.pets.data.local.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetDetailViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val miembroDao: MiembroDao,
  private val mascotaDao: MascotaDao
) : ViewModel() {

  private val petId: Long = checkNotNull(savedStateHandle["petId"])

  val pet: StateFlow<MiembroEntity?> = viewModelScope.let {
    // En una app real usaríamos un Flow de miembroDao
    kotlinx.coroutines.flow.flow {
      emit(miembroDao.getMiembroById(petId))
    }.stateIn(it, SharingStarted.WhileSubscribed(5000), null)
  }

  val pesos: StateFlow<List<MascotaPesoEntity>> = mascotaDao.getPesos(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val vacunas: StateFlow<List<MascotaVacunaEntity>> = mascotaDao.getVacunas(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val medicaciones: StateFlow<List<MascotaMedicacionEntity>> = mascotaDao.getMedicacionesActivas(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val desparasitaciones: StateFlow<List<MascotaDesparasitacionEntity>> = mascotaDao.getDesparasitaciones(petId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  fun addPeso(valor: Double) {
    viewModelScope.launch {
      mascotaDao.insertPeso(
        MascotaPesoEntity(
          mascotaId = petId,
          pesoKg = valor,
          fecha = System.currentTimeMillis()
        )
      )
    }
  }

  fun deletePeso(peso: MascotaPesoEntity) {
    viewModelScope.launch {
      mascotaDao.deletePeso(peso)
    }
  }

  fun addMedicacion(nombre: String, dosis: String, frecuencia: String) {
    viewModelScope.launch {
      mascotaDao.insertMedicacion(
        MascotaMedicacionEntity(
          mascotaId = petId,
          nombre = nombre,
          dosis = dosis,
          frecuencia = frecuencia,
          fechaInicio = System.currentTimeMillis()
        )
      )
    }
  }

  fun updateMedicacion(med: MascotaMedicacionEntity) {
    viewModelScope.launch {
      mascotaDao.insertMedicacion(med)
    }
  }

  fun deleteMedicacion(med: MascotaMedicacionEntity) {
    viewModelScope.launch {
      mascotaDao.deleteMedicacion(med)
    }
  }

  fun addVacuna(nombre: String) {
    viewModelScope.launch {
      mascotaDao.insertVacuna(
        MascotaVacunaEntity(
          mascotaId = petId,
          nombre = nombre,
          fechaAplicacion = System.currentTimeMillis()
        )
      )
    }
  }

  fun deleteVacuna(vacuna: MascotaVacunaEntity) {
    viewModelScope.launch {
      mascotaDao.deleteVacuna(vacuna)
    }
  }

  fun addDesparasitacion(tipo: String, producto: String) {
    viewModelScope.launch {
      mascotaDao.insertDesparasitacion(
        MascotaDesparasitacionEntity(
          mascotaId = petId,
          tipo = tipo,
          producto = producto,
          fechaAplicacion = System.currentTimeMillis()
        )
      )
    }
  }

  fun deleteDesparasitacion(item: MascotaDesparasitacionEntity) {
    viewModelScope.launch {
      mascotaDao.deleteDesparasitacion(item)
    }
  }
}
