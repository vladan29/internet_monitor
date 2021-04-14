package com.vladan.internetchecker

interface NetworkStateListener {
    fun onNetworkStateChanged(networkState: NetworkState)
}