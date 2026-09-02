package com.example.smritisetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.smritisetu.data.AuthManager
import com.example.smritisetu.theme.SmritiSetuTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val themeMode by AuthManager.themeMode.collectAsState()
      val fontScale by AuthManager.fontScale.collectAsState()
      val selectedLanguage by AuthManager.selectedLanguage.collectAsState()

      SmritiSetuTheme(
        themeMode = themeMode,
        fontScale = fontScale,
        selectedLanguage = selectedLanguage
      ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation()
        }
      }
    }
  }
}
