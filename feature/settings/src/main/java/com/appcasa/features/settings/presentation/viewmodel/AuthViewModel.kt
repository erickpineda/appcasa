package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.ui.utils.UiText
import com.appcasa.feature.settings.R
import com.google.firebase.auth.FirebaseAuth
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
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authEvent.emit(AuthEvent.Success)
            } catch (e: Exception) {
                val error = e.message?.let { UiText.DynamicString(it) } 
                    ?: UiText.StringResource(R.string.auth_error_unknown)
                _uiState.value = AuthUiState.Error(error)
            }
        }
    }

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                _authEvent.emit(AuthEvent.Success)
            } catch (e: Exception) {
                val error = e.message?.let { UiText.DynamicString(it) } 
                    ?: UiText.StringResource(R.string.auth_error_unknown)
                _uiState.value = AuthUiState.Error(error)
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
    }
}
