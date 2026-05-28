package com.appcasa

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.appcasa.core.ui.theme.AppCasaTheme
import com.appcasa.navigation.AppNavigation
import com.appcasa.presentation.viewmodel.GlobalViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val globalViewModel: GlobalViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    setContent {
      val isDarkMode by globalViewModel.isDarkMode.collectAsState()
      val isShopMode by globalViewModel.isShopMode.collectAsState()

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
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
  AppCasaTheme {
    AppNavigation()
  }
}
