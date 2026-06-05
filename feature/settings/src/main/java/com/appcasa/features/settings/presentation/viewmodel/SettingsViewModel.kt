package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.usecase.*
import com.appcasa.features.settings.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentHouseholdUseCase: GetCurrentHouseholdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getActiveListsUseCase: GetActiveListsUseCase,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val updateConfigurationUseCase: UpdateConfigurationUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val regenerateHouseCodeUseCase: RegenerateHouseCodeUseCase,
    private val updateHouseholdUseCase: UpdateHouseholdUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val hogarActual: StateFlow<Household?> = getCurrentHouseholdUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usuarioActual: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAdmin: StateFlow<Boolean> = usuarioActual.map { it?.rol == RolHogar.ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todasLasListas: StateFlow<List<Lista>> = hogarActual.flatMapLatest { hogar ->
        hogar?.let { getActiveListsUseCase(it.id, 1) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _configuraciones = MutableStateFlow<Map<String, String>>(emptyMap())
    val configuraciones: StateFlow<Map<String, String>> = _configuraciones.asStateFlow()

    init {
        viewModelScope.launch {
            hogarActual.collect { hogar ->
                hogar?.let {
                    getConfigurationUseCase(it.id).collect { configs ->
                        _configuraciones.value = configs.associate { it.clave to it.valor }
                    }
                }
            }
        }
    }

    fun updateConfig(clave: String, valor: String) {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            updateConfigurationUseCase(hogar.id, clave, valor)
        }
    }

    fun updateUsuario(nombre: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            val usuario = usuarioActual.value ?: return@launch
            updateUserUseCase(usuario, nombre, avatarUrl)
        }
    }

    fun regenerateHouseCode() {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            if (isAdmin.value) {
                regenerateHouseCodeUseCase(hogar.id)
            }
        }
    }

    fun updateHogar(nombre: String) {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            updateHouseholdUseCase(hogar, nombre)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
