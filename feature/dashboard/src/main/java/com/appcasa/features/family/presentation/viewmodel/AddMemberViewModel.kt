package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMemberViewModel @Inject constructor(
  private val miembroDao: MiembroDao,
  private val configuracionDao: ConfiguracionDao,
  private val eventoDao: EventoDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  fun addMember(
    nombre: String, 
    tipo: TipoMiembro, 
    raza: String? = null, 
    color: String? = null,
    chip: String? = null, 
    fotoUri: String? = null,
    fechaNacimiento: Long? = null
  ) {
    viewModelScope.launch {
      miembroDao.insertMiembro(
        MiembroEntity(
          hogarId = householdId,
          nombre = nombre,
          tipo = tipo.name,
          raza = raza,
          colorPelaje = color,
          numeroChip = chip,
          fotoUri = fotoUri,
          fechaNacimiento = fechaNacimiento
        )
      )
      
      // Crear evento de cumpleaños automático
      if (fechaNacimiento != null) {
        eventoDao.insertEvento(
          EventoEntity(
            hogarId = householdId,
            titulo = "Cumpleaños: $nombre 🎂",
            fecha = fechaNacimiento,
            tipo = TipoEvento.CUMPLEANOS.name
          )
        )
      }
    }
  }
}
