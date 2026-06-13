package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.ui.utils.UiText
import com.appcasa.feature.settings.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    fun login(email: String, pass: String) {
        if (!isEmailValid(email)) {
            _uiState.value = AuthUiState.Error(UiText.StringResource(R.string.auth_error_invalid_email))
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authEvent.emit(AuthEvent.Success)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(parseFirebaseAuthError(e))
            }
        }
    }

    fun register(email: String, pass: String) {
        if (!isEmailValid(email)) {
            _uiState.value = AuthUiState.Error(UiText.StringResource(R.string.auth_error_invalid_email))
            return
        }
        if (pass.length < 8) {
            _uiState.value = AuthUiState.Error(UiText.StringResource(R.string.auth_error_weak_password))
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                _authEvent.emit(AuthEvent.Success)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(parseFirebaseAuthError(e))
            }
        }
    }

    fun sendResetPassword(email: String) {
        if (!isEmailValid(email)) {
            _uiState.value = AuthUiState.Error(UiText.StringResource(R.string.auth_error_invalid_email))
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = AuthUiState.Idle
                _authEvent.emit(AuthEvent.Message(UiText.StringResource(R.string.settings_email_verification_sent)))
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(parseFirebaseAuthError(e))
            }
        }
    }

    fun onGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _authEvent.emit(AuthEvent.Success)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(parseFirebaseAuthError(e))
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun parseFirebaseAuthError(e: Exception): UiText {
        e.printStackTrace() // Loggear el error real para depuración
        return when (e) {
            is FirebaseAuthInvalidUserException -> UiText.StringResource(R.string.auth_error_user_not_found)
            is FirebaseAuthInvalidCredentialsException -> UiText.StringResource(R.string.auth_error_wrong_password)
            is FirebaseAuthUserCollisionException -> UiText.StringResource(R.string.auth_error_email_already_in_use)
            is FirebaseAuthWeakPasswordException -> UiText.StringResource(R.string.auth_error_weak_password)
            is FirebaseNetworkException -> UiText.StringResource(R.string.auth_error_network)
            is FirebaseAuthRecentLoginRequiredException -> UiText.StringResource(R.string.auth_error_reauth_required)
            else -> {
                val msg = e.localizedMessage ?: ""
                if (msg.contains("CONFIGURATION_NOT_FOUND")) {
                    UiText.StringResource(R.string.auth_error_config_missing)
                } else {
                    UiText.StringResource(R.string.auth_error_unknown)
                }
            }
        }
    }

    sealed interface AuthUiState {
        data object Idle : AuthUiState
        data object Loading : AuthUiState
        data class Error(val message: UiText) : AuthUiState
    }

    sealed interface AuthEvent {
        data object Success : AuthEvent
        data class Message(val text: UiText) : AuthEvent
    }
}
