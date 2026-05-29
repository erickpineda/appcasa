package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.calendar.data.local.EventoDao
import com.appcasa.features.calendar.data.local.EventoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
  private val miembroDao: MiembroDao,
  private val eventoDao: EventoDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  val familyMembers: StateFlow<List<MiembroEntity>> = miembroDao.getMiembrosByHogar(householdId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val people: StateFlow<List<MiembroEntity>> = familyMembers.map { list ->
    list.filter { it.tipo == TipoMiembro.PERSONA.name }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pets: StateFlow<List<MiembroEntity>> = familyMembers.map { list ->
    list.filter { it.tipo != TipoMiembro.PERSONA.name }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun deleteMember(member: MiembroEntity) {
    viewModelScope.launch {
      miembroDao.deleteMiembro(member)
    }
  }

  fun syncBirthdayEvent(member: MiembroEntity) {
    viewModelScope.launch {
      if (member.fechaNacimiento != null) {
        val titulo = "Cumpleaños: ${member.nombre} 🎂"
        
        eventoDao.insertEvento(
          EventoEntity(
            hogarId = householdId,
            titulo = titulo,
            fecha = member.fechaNacimiento!!,
            tipo = TipoEvento.CUMPLEANOS.name
          )
        )
      }
    }
  }
}
