package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.usecase.household.GetMemberByIdUseCase
import com.appcasa.core.domain.usecase.household.UpdateMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemberViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getMemberByIdUseCase: GetMemberByIdUseCase,
  private val updateMemberUseCase: UpdateMemberUseCase
) : ViewModel() {

  private val memberId: String = checkNotNull(savedStateHandle["memberId"])

  private val _member = MutableStateFlow<FamilyMember?>(null)
  val member: StateFlow<FamilyMember?> = _member.asStateFlow()

  init {
    loadMember()
  }

  private fun loadMember() {
    viewModelScope.launch {
      _member.value = getMemberByIdUseCase(memberId)
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
    avatarUrl: String? = null,
    fechaNacimiento: Long? = null
  ) {
    viewModelScope.launch {
      _member.value?.let { current ->
        val updated = current.copy(
          nombre = nombre,
          tipo = tipo,
          raza = raza,
          colorPelaje = color,
          numeroChip = chip,
          veterinarioNombre = vetNombre,
          veterinarioTelefono = vetTlf,
          avatarUrl = avatarUrl ?: current.avatarUrl,
          fechaNacimiento = fechaNacimiento,
          updatedAt = System.currentTimeMillis()
        )
        updateMemberUseCase(updated)
      }
    }
  }
}
