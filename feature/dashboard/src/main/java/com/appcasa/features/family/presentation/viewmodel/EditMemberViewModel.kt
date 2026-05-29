package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemberViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val miembroDao: MiembroDao,
  private val eventoDao: EventoDao
) : ViewModel() {

  private val memberId: Long = checkNotNull(savedStateHandle["memberId"])

  private val _member = MutableStateFlow<MiembroEntity?>(null)
  val member: StateFlow<MiembroEntity?> = _member.asStateFlow()

  init {
    loadMember()
  }

  private fun loadMember() {
    viewModelScope.launch {
      _member.value = miembroDao.getMiembroById(memberId)
    }
  }

  fun updateMember(
    nombre: String, 
    tipo: TipoMiembro, 
    raza: String? = null, 
    color: String? = null,
    chip: String? = null, 
    vetNombre: String? = null, 
    vetTlf: String? = null, 
    fotoUri: String? = null,
    fechaNacimiento: Long? = null
  ) {
    viewModelScope.launch {
      _member.value?.let { current ->
        val updated = current.copy(
          nombre = nombre,
          tipo = tipo.name,
          raza = raza,
          colorPelaje = color,
          numeroChip = chip,
          veterinarioNombre = vetNombre,
          veterinarioTelefono = vetTlf,
          fotoUri = fotoUri ?: current.fotoUri,
          fechaNacimiento = fechaNacimiento,
          updatedAt = System.currentTimeMillis()
        )
        miembroDao.updateMiembro(updated)
        
        // Sincronizar evento de cumpleaños
        if (fechaNacimiento != null) {
          eventoDao.insertEvento(
            EventoEntity(
              hogarId = current.hogarId,
              titulo = "Cumpleaños: $nombre 🎂",
              fecha = fechaNacimiento,
              tipo = TipoEvento.CUMPLEANOS.name
            )
          )
        }
      }
    }
  }
}
