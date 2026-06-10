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
    
    // Inicializar el canal de notificaciones al arrancar
    NotificationHelper(this)

    setContent {
      val isDarkMode by globalViewModel.isDarkMode.collectAsState()
      val isShopMode by globalViewModel.isShopMode.collectAsState()

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
        AppNavigation()
      }
    }
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
