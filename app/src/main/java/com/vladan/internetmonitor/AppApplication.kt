package com.vladan.internetmonitor

import android.app.Application
import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import org.greenrobot.eventbus.EventBus

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

    private val aliveNetworks: ArrayList<Network> = arrayListOf()
    private var maxTimeToLive: Int = -1
    private var losingNetwork: Network? = null
    private var networkType: Int = -1
    private var availableNetwork: Network? = null
    private var isConnected: Boolean = false
    private var signalStrength: Int = 1000
    private var linkDnBandwidth: Int = -1
    private var linkUpBandwidth: Int = -1
    private var connectivityDispatcher: ConnectivityManager? = null
    private var builder: NetworkRequest.Builder? = null
    private var networkRequest: NetworkRequest? = null
    private var isCurrentNetworkLost: Boolean = false
    private lateinit var wifiManager: WifiManager

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            //TODO This method works only for API level above 28 (introduced at 29).
        }

        /** Starting with Build.VERSION_CODES.O this method is guaranteed to be called
         * immediately after onAvailable(Network).*/
        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isConnected = (networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                ))
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkType = NetworkCapabilities.TRANSPORT_WIFI
                signalStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    networkCapabilities.signalStrength
                }
                else {
                    wifiManager.connectionInfo.rssi
                }
            }
            else {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    networkType = NetworkCapabilities.TRANSPORT_CELLULAR

                }
            }
            linkDnBandwidth = networkCapabilities.linkDownstreamBandwidthKbps
            linkUpBandwidth = networkCapabilities.linkUpstreamBandwidthKbps

            Log.d(TAG, "Network capabilities: $networkCapabilities, IsConnected: $isConnected")
            deliverEvent()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            if (network == losingNetwork) {
                maxTimeToLive = -1
                losingNetwork = null
            }
            if (aliveNetworks.size == 1) {
                aliveNetworks.remove(network)
            }
            else
                if (aliveNetworks.size > 1) {
                    for (aliveNetwork in aliveNetworks) {
                        if (aliveNetwork == network) {
                            aliveNetworks.remove(aliveNetwork)
                            Log.d(TAG, "Method onLost removed: $network, $aliveNetworks")
                            break
                        }
                    }
                }
            if (aliveNetworks.size == 0) {
                isConnected = false
                maxTimeToLive = -1
                networkType = -1
                availableNetwork = null
                signalStrength = 1000
                linkDnBandwidth = -1
                linkUpBandwidth = -1
                deliverEvent()
            }
            Log.d(TAG, "Method onLost isConnected: $isConnected  $aliveNetworks")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            if (connectivityDispatcher?.getNetworkCapabilities(network)!!
                    .hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.d(TAG, "Link properties: $linkProperties")
            }
        }

        /**This method is called if no network is found within the timeout time specified in
         * ConnectivityManager.requestNetwork(android.net.NetworkRequest, android.net.ConnectivityManager.NetworkCallback, int)
         * call or if the requested network request cannot be fulfilled (whether or not a timeout was specified).
         * When this callback is invoked the associated NetworkRequest will have already been removed and released,
         * as if ConnectivityManager.unregisterNetworkCallback(android.net.ConnectivityManager.NetworkCallback) had been called.
         * */
        override fun onUnavailable() {
            super.onUnavailable()
            Log.d(TAG, "onUnavailable is triggered")
            //TODO This method works only for API level above 25 (introduced at 26).
            if (aliveNetworks.isNotEmpty())
                aliveNetworks.clear()
        }

        /**Use this method if want to react with some action during @maxMsToLive time.
         * Keep at mind that this method is triggered only when we have two networks
         * and one switch to other.When we suddenly lose network this method isn't triggered.*/
        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            losingNetwork = network
            maxTimeToLive = maxMsToLive
            Log.d(TAG, "Method onLosing network: $network max time to live: $maxMsToLive")
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            aliveNetworks.add(network)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                isConnected = true
                connectivityDispatcher?.getNetworkCapabilities(network)?.let {
                    if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        networkType = NetworkCapabilities.TRANSPORT_WIFI
                    }

                    if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        networkType = NetworkCapabilities.TRANSPORT_CELLULAR
                    }
                    deliverEvent()
                }
            }

            availableNetwork = network
            Log.d(
                TAG,
                "Method OnAvailable: $aliveNetworks , AvailableNetwork: $availableNetwork, $isConnected"
            )

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
        Log.d(TAG, "Is connected: $isConnected")
    }

    private fun deliverEvent() {
        EventBus.getDefault()
            .postSticky(
                NetworkState(
                    isConnected,
                    networkType,
                    maxTimeToLive,
                    signalStrength,
                    linkDnBandwidth,
                    linkUpBandwidth
                )
            )
    }

}