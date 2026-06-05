package com.appcasa.features.utilities.presentation.viewmodel

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.Document
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.documents.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartSafeViewModel @Inject constructor(
  private val getDocumentsUseCase: GetDocumentsUseCase,
  private val addDocumentUseCase: AddDocumentUseCase,
  private val deleteDocumentUseCase: DeleteDocumentUseCase,
  private val updateDocumentUseCase: UpdateDocumentUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _isUnlocked = MutableStateFlow(false)
  val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

  private val householdId = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val documentos: StateFlow<List<Document>> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      getDocumentsUseCase(id)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun authenticate(activity: FragmentActivity) {
    try {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        val canAuth = biometricManager.canAuthenticate(authenticators)
        
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso al Baúl")
                .setSubtitle("Usa tu huella o cara para entrar")
                .setAllowedAuthenticators(authenticators)
                .build()

            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _isUnlocked.value = true
                }
            })
            biometricPrompt.authenticate(promptInfo)
        } else {
            _isUnlocked.value = true
        }
    } catch (e: Exception) {
        _isUnlocked.value = true
    }
  }

  fun addDocumento(
    nombre: String,
    categoria: String,
    uriPdf: String,
    vencimiento: Long? = null
  ) {
    viewModelScope.launch {
      addDocumentUseCase(householdId, nombre, categoria, uriPdf, vencimiento)
    }
  }

  fun deleteDocumento(documento: Document) {
    viewModelScope.launch {
      deleteDocumentUseCase(documento)
    }
  }

  fun updateDocumento(documento: Document) {
    viewModelScope.launch {
      updateDocumentUseCase(documento)
    }
  }
  
  fun uploadToCloud(documento: Document) {
    viewModelScope.launch {
        // Mock cloud upload logic
        val mockCloudUrl = "https://drive.google.com/mock/${documento.nombre}"
        updateDocumentUseCase(documento.copy(urlNube = mockCloudUrl, sincronizado = true))
    }
  }
}
