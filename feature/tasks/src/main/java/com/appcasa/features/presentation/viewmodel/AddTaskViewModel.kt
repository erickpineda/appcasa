package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.GetCurrentUserUseCase
import com.appcasa.core.domain.usecase.GetFamilyMembersUseCase
import com.appcasa.features.tasks.domain.usecase.AddTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
  private val addTaskUseCase: AddTaskUseCase,
  private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
  private val getCurrentUserUseCase: GetCurrentUserUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val familyMembers: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
    .flatMapLatest { getFamilyMembersUseCase(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  fun addTask(
    titulo: String, 
    prioridad: Prioridad, 
    asignadoId: Long? = null, 
    esPersonal: Boolean = false, 
    fotoUri: String? = null,
    fechaLimite: Long? = null,
    anticipacionMins: Int = 0,
    periodicidad: Periodicidad = Periodicidad.NINGUNA,
    tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA
  ) {
    viewModelScope.launch {
      val currentUser = getCurrentUserUseCase().first()

      addTaskUseCase(
        hogarId = householdId,
        titulo = titulo,
        prioridad = prioridad,
        asignadoId = asignadoId,
        esPersonal = esPersonal,
        fotoUri = fotoUri,
        fechaLimite = fechaLimite,
        anticipacionMins = anticipacionMins,
        periodicidad = periodicidad,
        tipoContenido = tipoContenido,
        createdById = currentUser?.id
      )
    }
  }
}
