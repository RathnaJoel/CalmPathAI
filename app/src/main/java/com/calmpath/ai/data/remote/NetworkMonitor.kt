package com.calmpath.ai.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkStatus {
    ONLINE,
    OFFLINE
}

/**
 * Monitors real-time network connectivity status using Android ConnectivityManager (CO5).
 * Uses registerDefaultNetworkCallback for immediate and accurate network tracking.
 */
class NetworkMonitor(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "NetworkMonitor"
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _networkStatus = MutableStateFlow(checkInitialStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _isOnlineFlow = MutableStateFlow(_networkStatus.value == NetworkStatus.ONLINE)
    val isOnlineFlow: StateFlow<Boolean> = _isOnlineFlow.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkInitialStatus(): NetworkStatus {
        return if (isOnline()) NetworkStatus.ONLINE else NetworkStatus.OFFLINE
    }

    fun isOnline(): Boolean {
        return try {
            val cm = connectivityManager ?: return true // Optimistic default
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.e(tag, "Error checking network status: ${e.message}")
            true // Optimistic fallback: allow HTTP requests to attempt
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = connectivityManager ?: return
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(tag, "Network is available (ONLINE)")
                    updateStatus(NetworkStatus.ONLINE)
                }

                override fun onLost(network: Network) {
                    Log.d(tag, "Network lost (OFFLINE)")
                    updateStatus(NetworkStatus.OFFLINE)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    updateStatus(if (hasInternet) NetworkStatus.ONLINE else NetworkStatus.OFFLINE)
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Failed to register default network callback", e)
        }
    }

    private fun updateStatus(status: NetworkStatus) {
        _networkStatus.value = status
        _isOnlineFlow.value = (status == NetworkStatus.ONLINE)
    }
}
