package com.calmpath.ai.alerts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.calmpath.ai.MainActivity
import com.calmpath.ai.data.local.entities.SmartAlertEntity

/**
 * Android Notification Manager and Channel Dispatcher (CO10).
 * Handles notification channels, interactive action buttons, deep-linking PendingIntents,
 * and Android 13+ permission safety.
 */
class NotificationHelper(
    private val context: Context
) {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 1: Environmental Alerts (High priority)
            val envChannel = NotificationChannel(
                CHANNEL_ENVIRONMENTAL,
                "CalmPath Environmental Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for air quality, noise spikes, and sudden weather shifts"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(envChannel)

            // Channel 2: Recommendations (Default priority)
            val recChannel = NotificationChannel(
                CHANNEL_RECOMMENDATIONS,
                "CalmPath Recommendations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Personalized peaceful place recommendations powered by CalmPath ML"
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(recChannel)
        }
    }

    /**
     * Dispatches an interactive Android notification.
     */
    fun showNotification(
        alertId: String,
        type: String,
        title: String,
        message: String,
        priority: String,
        targetScreen: String = "explore",
        placeId: String? = null,
        placeLatitude: Double? = null,
        placeLongitude: Double? = null
    ): Boolean {
        // Android 13+ permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        val channelId = if (type == SmartAlertEntity.TYPE_ML_RECOMMENDATION || type == SmartAlertEntity.TYPE_PEACEFUL_PLACE_ALERT) {
            CHANNEL_RECOMMENDATIONS
        } else {
            CHANNEL_ENVIRONMENTAL
        }

        // Primary click intent -> MainActivity with target_screen extra
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_SCREEN, targetScreen)
            putExtra(EXTRA_ALERT_ID, alertId)
            placeId?.let { putExtra(EXTRA_PLACE_ID, it) }
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            alertId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (priority == SmartAlertEntity.PRIORITY_HIGH) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        // Action Buttons based on alert type
        when (type) {
            SmartAlertEntity.TYPE_AQI_ALERT, SmartAlertEntity.TYPE_NOISE_ALERT -> {
                // Action 1: Explore Places
                val exploreIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_TARGET_SCREEN, "explore")
                }
                val explorePending = PendingIntent.getActivity(
                    context,
                    (alertId + "_action1").hashCode(),
                    exploreIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_menu_search, "Explore Places", explorePending)

                // Action 2: Open CalmPath
                val homeIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_TARGET_SCREEN, "home")
                }
                val homePending = PendingIntent.getActivity(
                    context,
                    (alertId + "_action2").hashCode(),
                    homeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_menu_compass, "Open CalmPath", homePending)
            }

            SmartAlertEntity.TYPE_ML_RECOMMENDATION -> {
                // Action 1: View Place
                if (placeId != null) {
                    val viewIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_TARGET_SCREEN, "place_details/$placeId")
                        putExtra(EXTRA_PLACE_ID, placeId)
                    }
                    val viewPending = PendingIntent.getActivity(
                        context,
                        (alertId + "_view").hashCode(),
                        viewIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(android.R.drawable.ic_menu_view, "View Place", viewPending)
                }

                // Action 2: Navigate via Google Maps
                if (placeLatitude != null && placeLongitude != null) {
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$placeLatitude,$placeLongitude&mode=w")).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    val navPending = PendingIntent.getActivity(
                        context,
                        (alertId + "_nav").hashCode(),
                        mapIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(android.R.drawable.ic_menu_directions, "Navigate", navPending)
                }
            }

            SmartAlertEntity.TYPE_WEATHER_ALERT -> {
                // Action 1: Explore Indoor Places
                val exploreIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_TARGET_SCREEN, "explore")
                }
                val explorePending = PendingIntent.getActivity(
                    context,
                    (alertId + "_weather1").hashCode(),
                    exploreIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_menu_search, "Indoor Sanctuaries", explorePending)
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(alertId.hashCode(), builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    companion object {
        const val CHANNEL_ENVIRONMENTAL = "channel_environmental_alerts"
        const val CHANNEL_RECOMMENDATIONS = "channel_recommendations"

        const val EXTRA_TARGET_SCREEN = "target_screen"
        const val EXTRA_ALERT_ID = "alert_id"
        const val EXTRA_PLACE_ID = "place_id"
    }
}
