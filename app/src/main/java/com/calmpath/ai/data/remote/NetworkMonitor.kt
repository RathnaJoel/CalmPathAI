package com.calmpath.ai.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
 * Emits reactive StateFlow updates for Compose UI and provides synchronous queries.
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
            val cm = connectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (e: Exception) {
            Log.e(tag, "Error checking network status: ${e.message}")
            false
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = connectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(tag, "Network became available")
                    updateStatus(NetworkStatus.ONLINE)
                }

                override fun onLost(network: Network) {
                    Log.d(tag, "Network connection lost")
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
            Log.e(tag, "Failed to register network callback", e)
        }
    }

    private fun updateStatus(status: NetworkStatus) {
        _networkStatus.value = status
        _isOnlineFlow.value = (status == NetworkStatus.ONLINE)
    }
}
