package com.appcasa.features.utilities.presentation.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.scheduler.ReminderScheduler
import com.appcasa.features.documents.data.local.DocumentoDao
import com.appcasa.features.documents.data.local.DocumentoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
  private val documentoDao: DocumentoDao,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
  private val reminderScheduler: ReminderScheduler,
  @ApplicationContext private val context: Context
) : ViewModel() {

  private val _isUnlocked = MutableStateFlow(false)
  val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

  private val householdId = currentHouseholdProvider.getCurrentHouseholdId()

  val documentos: StateFlow<List<DocumentoEntity>> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      documentoDao.getDocumentosByHogar(id)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun authenticate(activity: FragmentActivity) {
    val biometricManager = BiometricManager.from(context)
    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso al Baúl")
            .setSubtitle("Usa tu huella o cara para entrar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _isUnlocked.value = true
            }
        })
        biometricPrompt.authenticate(promptInfo)
    } else {
        // Si no hay biométricos, desbloqueamos (para el emulador o dispositivos antiguos)
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
      val doc = DocumentoEntity(
        hogarId = householdId,
        nombre = nombre,
        categoria = categoria,
        uriPdf = uriPdf,
        fechaVencimiento = vencimiento
      )
      val id = documentoDao.insertDocumento(doc)
      
      // Si tiene vencimiento, programar aviso (ej: 30 días antes)
      vencimiento?.let { date ->
        val alertDate = date - (30L * 24 * 60 * 60 * 1000)
        if (alertDate > System.currentTimeMillis()) {
          reminderScheduler.scheduleReminder(
            id = (id + 30000).toInt(),
            title = "Vencimiento Próximo: $nombre",
            message = "El documento de la categoría $categoria caduca en 30 días.",
            timeInMillis = alertDate
          )
        }
      }
    }
  }

  fun deleteDocumento(documento: DocumentoEntity) {
    viewModelScope.launch {
      documentoDao.deleteDocumento(documento)
      reminderScheduler.cancelReminder((documento.id + 30000).toInt())
    }
  }

  fun updateDocumento(documento: DocumentoEntity) {
    viewModelScope.launch {
      documentoDao.insertDocumento(documento)
      
      // Actualizar alerta si cambió la fecha
      reminderScheduler.cancelReminder((documento.id + 30000).toInt())
      documento.fechaVencimiento?.let { date ->
        val alertDate = date - (30L * 24 * 60 * 60 * 1000)
        if (alertDate > System.currentTimeMillis()) {
          reminderScheduler.scheduleReminder(
            id = (documento.id + 30000).toInt(),
            title = "Vencimiento Próximo: ${documento.nombre}",
            message = "El documento de la categoría ${documento.categoria} caduca en 30 días.",
            timeInMillis = alertDate
          )
        }
      }
    }
  }
  
  fun uploadToCloud(documento: DocumentoEntity) {
    // Placeholder para futura integración con Google Drive
    viewModelScope.launch {
        // Lógica de subida aquí
        val mockCloudUrl = "https://drive.google.com/mock/${documento.nombre}"
        documentoDao.insertDocumento(documento.copy(urlNube = mockCloudUrl, sincronizado = true))
    }
  }
}
