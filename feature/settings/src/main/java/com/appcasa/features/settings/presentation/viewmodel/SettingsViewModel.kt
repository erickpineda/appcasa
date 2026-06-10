package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.usecase.lists.GetActiveListsUseCase
import com.appcasa.core.domain.usecase.config.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.config.UpdateConfigurationUseCase
import com.appcasa.core.domain.usecase.user.GetCurrentUserUseCase
import com.appcasa.features.settings.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import com.appcasa.core.ui.utils.UiText
import com.appcasa.feature.settings.R
import kotlinx.coroutines.flow.*
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
    private val forceSyncUseCase: ForceSyncUseCase,
    private val exportHouseholdDataUseCase: ExportHouseholdDataUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _settingsEvent = MutableSharedFlow<SettingsUiEvent>()
    val settingsEvent = _settingsEvent.asSharedFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

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

    fun forceSync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            _isSyncing.value = true
            forceSyncUseCase(hogar.id)
            _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_sync_started)))
            // Simulamos un tiempo de "proceso" para dar feedback y evitar spam
            kotlinx.coroutines.delay(3000)
            _isSyncing.value = false
        }
    }

    fun exportData() {
        if (_isExporting.value) return
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            _isExporting.value = true
            val data = exportHouseholdDataUseCase(hogar.id)
            _settingsEvent.emit(SettingsUiEvent.ExportReady(data))
            _isExporting.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}

sealed interface SettingsUiEvent {
    data class ShowToast(val message: UiText) : SettingsUiEvent
    data class ExportReady(val content: String) : SettingsUiEvent
}
