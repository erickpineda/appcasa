package com.appcasa.features.settings.presentation.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.usecase.lists.GetActiveListsUseCase
import com.appcasa.core.domain.usecase.config.GetConfigurationUseCase
import com.appcasa.core.domain.usecase.config.UpdateConfigurationUseCase
import com.appcasa.core.domain.usecase.user.GetCurrentUserUseCase
import com.appcasa.core.ui.utils.UiText
import com.appcasa.feature.settings.R
import com.appcasa.features.settings.domain.usecase.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val switchProfileUseCase: SwitchProfileUseCase,
    private val linkAccountUseCase: LinkAccountUseCase,
    private val getAllHouseholdsUseCase: GetAllHouseholdsUseCase,
    private val switchHouseholdUseCase: SwitchHouseholdUseCase,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _firebaseUser = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), firebaseAuth.currentUser)

    val isLoggedIn: StateFlow<Boolean> = _firebaseUser.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), firebaseAuth.currentUser != null)

    val hogarActual: StateFlow<Household?> = getCurrentHouseholdUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usuarioActual: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isGoogleAccount: StateFlow<Boolean> = combine(_firebaseUser, usuarioActual) { firebaseUser, user ->
        val hasGoogleInFirebase = firebaseUser?.providerData?.any { it.providerId.contains("google") } ?: false
        val isLinkedWithGoogleId = user?.authId?.isNotBlank() == true && user.email.contains("@appcasa.local") == false
        
        hasGoogleInFirebase || isLinkedWithGoogleId
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        firebaseAuth.currentUser?.providerData?.any { it.providerId.contains("google") } ?: false
    )

    private val _settingsEvent = MutableSharedFlow<SettingsUiEvent>()
    val settingsEvent = _settingsEvent.asSharedFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

    private val _showLinkAccountDialog = MutableStateFlow(false)
    val showLinkAccountDialog = _showLinkAccountDialog.asStateFlow()

    fun dismissLinkAccountDialog() {
        _showLinkAccountDialog.value = false
    }

    val isAdmin: StateFlow<Boolean> = usuarioActual.map { it?.rol == RolHogar.ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todasLasListas: StateFlow<List<Lista>> = hogarActual.flatMapLatest { hogar ->
        hogar?.let { getActiveListsUseCase(it.id, 1) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosHogares: StateFlow<List<Household>> = getAllHouseholdsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        
        // Auto-vinculación si el usuario está logueado pero el perfil local es temporal
        viewModelScope.launch {
            combine(isLoggedIn, usuarioActual) { logged, user -> 
                logged && user?.email?.contains("@appcasa.local") == true
            }.collect { shouldPrompt ->
                if (shouldPrompt) {
                    _showLinkAccountDialog.value = true
                }
            }
        }
    }

    fun updateConfig(clave: String, valor: String) {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            updateConfigurationUseCase(hogar.id, clave, valor)
            
            if (clave == "biometric_lock_app") {
                sharedPrefs.edit().putBoolean("biometric_lock_app", valor == "true").apply()
            }
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

    fun switchHogar(id: String) {
        viewModelScope.launch {
            switchHouseholdUseCase(id)
        }
    }

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun linkAccount() {
        _showLinkAccountDialog.value = false
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                linkAccountUseCase()
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_sync_started)))
                _isSyncing.value = false
            } catch (e: Exception) {
                _isSyncing.value = false
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.DynamicString("Error al vincular: ${e.message}")))
            }
        }
    }

    fun forceSync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            _isSyncing.value = true
            forceSyncUseCase(hogar.id)
            _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_sync_started)))
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
            switchProfileUseCase()
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                firebaseAuth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_email_verification_sent)))
            } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_reauth_required_email)))
            } catch (e: Exception) {
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_error_generic)))
            }
        }
    }

    fun updatePassword(newPass: String) {
        if (newPass.length < 8) {
            viewModelScope.launch { _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_password_too_short))) }
            return
        }
        viewModelScope.launch {
            try {
                firebaseAuth.currentUser?.updatePassword(newPass)?.await()
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_password_updated)))
            } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_reauth_required_password)))
            } catch (e: Exception) {
                _settingsEvent.emit(SettingsUiEvent.ShowToast(UiText.StringResource(R.string.settings_error_generic)))
            }
        }
    }
}

sealed interface SettingsUiEvent {
    data class ShowToast(val message: UiText) : SettingsUiEvent
    data class ExportReady(val content: String) : SettingsUiEvent
}
