package com.calmpath.ai

import android.app.Application
import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.local.CalmPathDatabase
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.remote.FirestoreSyncManager
import com.calmpath.ai.data.remote.NetworkMonitor
import com.calmpath.ai.data.repository.AuthRepository
import com.calmpath.ai.data.repository.CalmPathRepository

/**
 * Application class initializing Room Database, Firebase Repositories,
 * Network Monitor, Location Services, and dependencies (CO3, CO4, CO5).
 */
class CalmPathApplication : Application() {

    lateinit var database: CalmPathDatabase
        private set

    lateinit var authManager: AuthManager
        private set

    lateinit var firestoreSyncManager: FirestoreSyncManager
        private set

    lateinit var networkMonitor: NetworkMonitor
        private set

    lateinit var locationHelper: LocationHelper
        private set

    lateinit var repository: CalmPathRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = CalmPathDatabase.getInstance(this)
        authManager = AuthManager()
        firestoreSyncManager = FirestoreSyncManager()
        networkMonitor = NetworkMonitor(this)
        locationHelper = LocationHelper(this)

        repository = CalmPathRepository(
            database = database,
            authManager = authManager,
            firestoreSync = firestoreSyncManager,
            networkMonitor = networkMonitor,
            locationHelper = locationHelper
        )

        authRepository = AuthRepository(authManager, database)
    }
}
