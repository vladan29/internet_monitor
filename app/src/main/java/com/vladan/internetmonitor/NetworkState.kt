package com.vladan.internetmonitor

/**
 * Created by vladan on 7/17/2020
 */
data class NetworkState(
    var isConnected: Boolean,
    var connectionType: Int,
    var networkSubtype: Int,
    var maxMsToLive : Int
)