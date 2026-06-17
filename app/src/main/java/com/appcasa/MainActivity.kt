package com.appcasa
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

  @Inject
  lateinit var sharedPrefs: SharedPreferences

  private val globalViewModel: GlobalViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // 1. Pre-flight check (Versión de DB/App)
    checkDatabaseHealth()
    
    // 2. Inicializar el canal de notificaciones
    NotificationHelper(this)

    setContent {
      val isDarkMode by globalViewModel.isDarkMode.collectAsStateWithLifecycle()
      val isShopMode by globalViewModel.isShopMode.collectAsStateWithLifecycle()
      val isSecureMode by globalViewModel.isSecureMode.collectAsStateWithLifecycle()
      val isHouseholdSetup by globalViewModel.isHouseholdSetup.collectAsStateWithLifecycle()
      
      var isAuthenticated by remember { mutableStateOf(false) }
      val isLockEnabled = remember(sharedPrefs) { sharedPrefs.getBoolean("biometric_lock_app", false) }

      // Manejo de Bloqueo Biométrico
      LaunchedEffect(isHouseholdSetup, isLockEnabled) {
          if (isHouseholdSetup == true && isLockEnabled && !isAuthenticated) {
              triggerBiometricPrompt {
                  isAuthenticated = true
              }
          }
      }

      // Solo bloqueamos si el hogar está configurado y el bloqueo está activo
      val shouldShowLockScreen = isHouseholdSetup == true && isLockEnabled && !isAuthenticated
      val shouldShowContent = isHouseholdSetup != null && !shouldShowLockScreen

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
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
              if (shouldShowContent) {
                  AppNavigation(globalViewModel = globalViewModel)
              } else if (shouldShowLockScreen) {
                  // Pantalla de bloqueo sólida para no ver el contenido de atrás
                  Box(
                      modifier = Modifier
                          .fillMaxSize()
                          .background(MaterialTheme.colorScheme.primary),
                      contentAlignment = Alignment.Center
                  ) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Icon(
                              imageVector = Icons.Default.Lock,
                              contentDescription = null,
                              modifier = Modifier.size(64.dp),
                              tint = MaterialTheme.colorScheme.onPrimary
                          )
                          Spacer(modifier = Modifier.height(16.dp))
                          Text(
                              text = getString(com.appcasa.core.ui.R.string.lock_app_title),
                              color = MaterialTheme.colorScheme.onPrimary,
                              style = MaterialTheme.typography.titleLarge
                          )
                          Spacer(modifier = Modifier.height(24.dp))
                          Button(
                              onClick = { 
                                  triggerBiometricPrompt { isAuthenticated = true }
                              },
                              colors = ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.onPrimary,
                                  contentColor = MaterialTheme.colorScheme.primary
                              )
                          ) {
                              Text(getString(com.appcasa.core.ui.R.string.lock_btn_unlock))
                          }
                      }
                  }
              }
              // Si isHouseholdSetup es null, no mostramos nada (Box vacío con fondo de sistema), 
              // evitando el parpadeo de la pantalla de bloqueo o de bienvenida antes de tiempo.
          }
      }
    }
  }

  private fun triggerBiometricPrompt(onSuccess: () -> Unit) {
      val executor = ContextCompat.getMainExecutor(this)
      val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
              super.onAuthenticationError(errorCode, errString)
              // No cerramos la app inmediatamente para permitir re-intento manual
          }
          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
              super.onAuthenticationSucceeded(result)
              onSuccess()
          }
      })

      val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle(getString(com.appcasa.core.ui.R.string.lock_app_title))
          .setSubtitle(getString(com.appcasa.core.ui.R.string.lock_app_subtitle))
          .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
          .build()

      biometricPrompt.authenticate(promptInfo)
  }

  private fun checkDatabaseHealth() {
      // Si en el futuro cambiamos drásticamente el esquema en la nube,
      // aquí podríamos forzar un logout o una migración destructiva.
      val currentVersion = 4
      val lastKnownVersion = sharedPrefs.getInt("db_schema_version", 0)
      
      if (lastKnownVersion != 0 && lastKnownVersion < currentVersion) {
          // Reservado para futuras limpiezas post-actualización
      }
      sharedPrefs.edit().putInt("db_schema_version", currentVersion).apply()
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

