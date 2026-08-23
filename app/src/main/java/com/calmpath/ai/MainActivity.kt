package com.calmpath.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.calmpath.ai.data.local.DatabaseSeeder
import com.calmpath.ai.ui.navigation.CalmPathNavHost
import com.calmpath.ai.ui.theme.CalmPathTheme

/**
 * Main Activity hosting CalmPath AI Jetpack Compose UI (CO1, CO2, CO3).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CalmPathApplication
        val repository = app.repository
        val authRepository = app.authRepository

        setContent {
            val settings by repository.settingsFlow.collectAsState(initial = DatabaseSeeder.defaultAppSettings)

            CalmPathTheme(themeMode = settings.theme) {
                CalmPathNavHost(
                    repository = repository,
                    authRepository = authRepository
                )
            }
        }
    }
}
