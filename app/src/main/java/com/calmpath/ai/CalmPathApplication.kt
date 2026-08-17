package com.calmpath.ai

import android.app.Application
import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.local.CalmPathDatabase
import com.calmpath.ai.data.remote.FirestoreSyncManager
import com.calmpath.ai.data.repository.AuthRepository
import com.calmpath.ai.data.repository.CalmPathRepository

/**
 * Application class initializing Room Database, Firebase Repositories, and dependencies.
 */
class CalmPathApplication : Application() {

    lateinit var database: CalmPathDatabase
        private set

    lateinit var authManager: AuthManager
        private set

    lateinit var firestoreSyncManager: FirestoreSyncManager
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

        repository = CalmPathRepository(
            database = database,
            authManager = authManager,
            firestoreSync = firestoreSyncManager
        )

        authRepository = AuthRepository(authManager)
    }
}
