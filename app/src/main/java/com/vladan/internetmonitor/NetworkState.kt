package com.vladan.internetmonitor

/**
 * Created by vladan on 7/17/2020
 */
data class NetworkState(
    var isConnected: Boolean,
    var connectionType: Int,
    var maxMsToLive: Int,
    var signalStrength: Int,
    var linkDnBandwidth: Int,
    var linkUpBandwidth: Int
)