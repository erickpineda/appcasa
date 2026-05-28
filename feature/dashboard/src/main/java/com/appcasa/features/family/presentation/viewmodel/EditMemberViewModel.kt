package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemberViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val miembroDao: MiembroDao
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

    fun updateMember(nombre: String, tipo: TipoMiembro, raza: String? = null, chip: String? = null, vetNombre: String? = null, vetTlf: String? = null, fotoUri: String? = null) {
        viewModelScope.launch {
            _member.value?.let { current ->
                miembroDao.updateMiembro(
                    current.copy(
                        nombre = nombre,
                        tipo = tipo.name,
                        raza = raza,
                        numeroChip = chip,
                        veterinarioNombre = vetNombre,
                        veterinarioTelefono = vetTlf,
                        fotoUri = fotoUri ?: current.fotoUri,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
