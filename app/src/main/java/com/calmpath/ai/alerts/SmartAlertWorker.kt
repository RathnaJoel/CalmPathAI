package com.calmpath.ai.alerts

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.calmpath.ai.CalmPathApplication
import java.util.concurrent.TimeUnit

/**
 * Android WorkManager CoroutineWorker for battery-efficient background alert checks (CO10).
 * Runs periodically (default 15 minutes) respecting OS background limits.
 */
class SmartAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "SmartAlertWorker"

    override suspend fun doWork(): Result {
        Log.d(tag, "Executing periodic SmartAlertWorker evaluation...")
        return try {
            val app = applicationContext as? CalmPathApplication
            if (app != null) {
                val repository = app.repository
                val smartAlertRepo = repository.smartAlertRepository
                smartAlertRepo?.evaluateAndTriggerAlerts(userId = "guest")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "SmartAlertWorker encountered exception: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "calmpath_smart_alerts_periodic_work"

        /**
         * Enqueues the periodic smart alert checking task.
         */
        fun enqueuePeriodicWork(context: Context, intervalMinutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SmartAlertWorker>(
                intervalMinutes.coerceAtLeast(15), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
