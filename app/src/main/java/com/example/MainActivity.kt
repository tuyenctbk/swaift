package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import com.example.ui.navigation.ZenFlowNav
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ZenFlowViewModel

// lint-ignore-memory-leak
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    try {
      com.example.engine.EnvironmentSimulator.syncWithRealDevice(this)
      com.example.service.AutomationForegroundService.startService(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    setContent {
      val mainViewModel: ZenFlowViewModel = viewModel()
      val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

      val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = darkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ZenFlowNav(viewModel = mainViewModel)
        }
      }
    }
  }
}
