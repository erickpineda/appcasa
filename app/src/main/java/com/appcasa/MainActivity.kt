package com.appcasa

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.appcasa.core.data.remote.manager.SyncManager
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.navigation.AppNavigation
import com.appcasa.presentation.viewmodel.GlobalViewModel
import com.appcasa.core.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

  @Inject
  lateinit var syncManager: SyncManager

  private val globalViewModel: GlobalViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // 1. Pre-flight check (Versión de DB/App)
    checkDatabaseHealth()
    
    // 2. Bloqueo Biométrico al arrancar (Opcional)
    checkBiometricLock()
    
    // Inicializar el canal de notificaciones al arrancar
    NotificationHelper(this)

    setContent {
      val isDarkMode by globalViewModel.isDarkMode.collectAsState()
      val isShopMode by globalViewModel.isShopMode.collectAsState()
      val isSecureMode by globalViewModel.isSecureMode.collectAsState()

      // Manejo de Modo Seguro (Protección de pantalla)
      LaunchedEffect(isSecureMode) {
          if (isSecureMode) {
              window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
          } else {
              window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
          }
      }

      // Solicitar permisos de notificación en Android 13+
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }
        
        LaunchedEffect(Unit) {
          launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }

      // Manejo de Modo Tienda (Pantalla encendida)
      if (isShopMode) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
      } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
      }

      AppCasaTheme(darkTheme = isDarkMode) {
        AppNavigation(globalViewModel = globalViewModel)
      }
    }
  }

  private fun checkDatabaseHealth() {
      // Si en el futuro cambiamos drásticamente el esquema en la nube,
      // aquí podríamos forzar un logout o una migración destructiva.
      val currentVersion = 4
      val lastKnownVersion = getSharedPreferences("app_casa_prefs", MODE_PRIVATE).getInt("db_schema_version", 0)
      
      if (lastKnownVersion != 0 && lastKnownVersion < currentVersion) {
          // Reservado para futuras limpiezas post-actualización
      }
      getSharedPreferences("app_casa_prefs", MODE_PRIVATE).edit().putInt("db_schema_version", currentVersion).apply()
  }

  private fun checkBiometricLock() {
      val isLockEnabled = getSharedPreferences("app_casa_prefs", MODE_PRIVATE).getBoolean("biometric_lock_app", false)
      if (!isLockEnabled) return
      
      val executor = ContextCompat.getMainExecutor(this)
      val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
              super.onAuthenticationError(errorCode, errString)
              if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                  finish() // Cerrar app si falla
              }
          }
          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
              super.onAuthenticationSucceeded(result)
          }
      })

      val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle(getString(com.appcasa.core.ui.R.string.lock_app_title))
          .setSubtitle(getString(com.appcasa.core.ui.R.string.lock_app_subtitle))
          .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
          .build()

      biometricPrompt.authenticate(promptInfo)
  }

  override fun onStart() {
    super.onStart()
    syncManager.setAppInForeground(true)
  }

  override fun onStop() {
    super.onStop()
    syncManager.setAppInForeground(false)
  }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
  AppCasaTheme {
    AppNavigation()
  }
}
