package com.calmpath.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.calmpath.ai.alerts.NotificationHelper
import com.calmpath.ai.data.local.DatabaseSeeder
import com.calmpath.ai.ui.navigation.CalmPathNavHost
import com.calmpath.ai.ui.theme.CalmPathTheme

/**
 * Main Activity hosting CalmPath AI Jetpack Compose UI (CO1 - CO10).
 * Handles runtime permissions and deep-link routing from notification clicks.
 */
class MainActivity : ComponentActivity() {

    private val targetRouteState = mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ Runtime Notification Permission Request (CO10)
        checkAndRequestNotificationPermission()

        // Extract deep-link target screen from notification intent
        handleNotificationIntent(intent)

        val app = application as CalmPathApplication
        val repository = app.repository
        val authRepository = app.authRepository

        setContent {
            val settings by repository.settingsFlow.collectAsState(initial = DatabaseSeeder.defaultAppSettings)
            val targetRoute by targetRouteState

            CalmPathTheme(themeMode = settings.theme) {
                CalmPathNavHost(
                    repository = repository,
                    authRepository = authRepository,
                    initialRoute = targetRoute
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_SCREEN)?.let { targetScreen ->
            targetRouteState.value = targetScreen
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
