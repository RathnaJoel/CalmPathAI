package com.calmpath.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.calmpath.ai.ui.navigation.CalmPathNavHost
import com.calmpath.ai.ui.theme.CalmPathTheme

/**
 * Main Activity hosting CalmPath AI Jetpack Compose UI (CO1 & CO2).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CalmPathApplication
        val repository = app.repository
        val authRepository = app.authRepository

        setContent {
            val preferences by repository.preferencesFlow.collectAsState(initial = com.calmpath.ai.data.local.entities.UserPreferencesEntity())

            CalmPathTheme(themeMode = preferences.themeMode) {
                CalmPathNavHost(
                    repository = repository,
                    authRepository = authRepository
                )
            }
        }
    }
}
