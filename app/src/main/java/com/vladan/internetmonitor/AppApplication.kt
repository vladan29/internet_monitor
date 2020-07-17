package com.vladan.internetmonitor

import android.app.Application
import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.util.Log

/**
 * Created by vladan on 7/15/2020
 */
// Used in the manifest
@Suppress("unused")
class AppApplication : Application() {

    companion object {
        private lateinit var instance: AppApplication
        private const val TAG = "AppApplication"

        fun getApplicationContext(): Context {
            return instance.applicationContext
        }
    }

    private var isFirstStart: Boolean = true
    private var networkType: Int = -1
    private var networkSubType: Int = -1
    private var availableNetwork: Network? = null
    private var currentNetwork: Network? = null
    private var isConnected: Boolean = false
    private var isLoosing: Boolean = false
    private var connectivityDispatcher: ConnectivityManager? = null
    private var builder: NetworkRequest.Builder? = null
    private var networkRequest: NetworkRequest? = null
    private var isCurrentNetworkLost: Boolean = false
    private lateinit var wifiManager: WifiManager

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
        }

        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            if (connectivityDispatcher?.getNetworkCapabilities(network)!!
                    .hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.d(TAG, "Link properties: $linkProperties")
            }
        }

        override fun onUnavailable() {
            super.onUnavailable()
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            super.onAvailable(network)
            availableNetwork = network
            if (isFirstStart) {
                currentNetwork = network
                isConnected = true
                checkNetworkTypeAndSubtype(network)
                deliverEvent()
                isFirstStart = false
                Log.d(
                    TAG, "Method: onAvailableFirstStart " +
                            "\nIs connected: $isConnected  " +
                            "\nAvailable network: $availableNetwork " +
                            "\nNetwork type: $networkType " +
                            "\nNetwork subtype: $networkSubType"
                )
            }
            else {
                if (isCurrentNetworkLost) {
                    currentNetwork = network
                    isConnected = true
                    isCurrentNetworkLost = false
                    checkNetworkTypeAndSubtype(network)
                    deliverEvent()
                    Log.d(
                        TAG, "Method: onAvailable " +
                                "\nIs connected: $isConnected  " +
                                "\nCurrent network: $currentNetwork " +
                                "\nNetwork type: $networkType " +
                                "\nNetwork subtype: $networkSubType"
                    )
                }
                else {
                    availableNetwork = network

                    Log.d(
                        TAG, "Method: onAvailablePartTwo " +
                                "\nAvailable network $availableNetwork " +
                                "\nCurrent network: $currentNetwork"
                    )
                }
            }
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        wifiManager =
            getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityDispatcher = applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager?
        builder = NetworkRequest.Builder()
        networkRequest = builder?.build()
        connectivityDispatcher?.registerNetworkCallback(
            networkRequest!!, networkCallback
        )
    }

    private fun deliverEvent() {

    }

    /** This deprecation annotation is only to wait to sdkVersion 30 */
    @Suppress("DEPRECATION")
    private fun checkNetworkTypeAndSubtype(network: Network) {
        if (connectivityDispatcher?.getNetworkCapabilities(network)!!.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )) {
            networkType = NetworkCapabilities.TRANSPORT_WIFI
            networkSubType = -1
        }
        else if (connectivityDispatcher?.getNetworkCapabilities(
                network
            )!!.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            )) {
            networkType = NetworkCapabilities.TRANSPORT_CELLULAR
            networkSubType = connectivityDispatcher?.activeNetworkInfo?.subtype!!
        }
    }
}