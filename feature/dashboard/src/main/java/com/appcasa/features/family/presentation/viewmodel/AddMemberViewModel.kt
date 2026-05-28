package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMemberViewModel @Inject constructor(
  private val miembroDao: MiembroDao,
  private val configuracionDao: ConfiguracionDao
) : ViewModel() {

  fun addMember(
    nombre: String, 
    tipo: TipoMiembro, 
    raza: String? = null, 
    color: String? = null,
    chip: String? = null, 
    fotoUri: String? = null
  ) {
    viewModelScope.launch {
      val hogarId = configuracionDao.getHogarActual().first()?.id ?: 1L
      miembroDao.insertMiembro(
        MiembroEntity(
          hogarId = hogarId,
          nombre = nombre,
          tipo = tipo.name,
          raza = raza,
          colorPelaje = color,
          numeroChip = chip,
          fotoUri = fotoUri
        )
      )
    }
  }
}
