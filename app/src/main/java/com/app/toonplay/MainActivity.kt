package com.app.toonplay

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.app.toonplay.ui.ToonPlayRoot
import com.app.toonplay.ui.state.HomeViewModel
import com.app.toonplay.ui.theme.ToonPlayTheme

class MainActivity : ComponentActivity() {

  private val homeViewModel: HomeViewModel by viewModels {
    val app = application as ToonPlayApplication
    HomeViewModel.Factory(
      repository = app.homeRepository,
      preferencesRepository = app.preferencesRepository,
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    setContent {
      ToonPlayTheme {
        ToonPlayRoot(viewModel = homeViewModel)
      }
    }
  }
}
