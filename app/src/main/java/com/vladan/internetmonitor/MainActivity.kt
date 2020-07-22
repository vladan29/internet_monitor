package com.vladan.internetmonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private var isConnected: Boolean = false
    private var isWifi: Boolean = false
    private var isCellular: Boolean = false
    private var linkDnBandwidth: Int = -1
    private var linkUpBandwidth: Int = -1
    private var signalStrength: Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        registerForNetworkEvent()

    }

    override fun onStart() {
        super.onStart()
        val stickyEvent: NetworkState? =
            EventBus.getDefault().getStickyEvent(NetworkState::class.java)
        stickyEvent?.let {
            setValues(it)
        }
        refreshViews()
    }

    override fun onPause() {
        super.onPause()
        unregisterForNetworkEvent()
    }
    /**This is the method in whose scope we can trigger every reaction
     * to the @NetworkState*/
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    fun onEvent(event: NetworkState) {
        setValues(event)
    }

    private fun setValues(event: NetworkState) {
        isConnected = event.isConnected
        isWifi = event.connectionType == 1
        isCellular = event.connectionType == 0
        linkDnBandwidth = event.linkDnBandwidth
        linkUpBandwidth = event.linkUpBandwidth
        signalStrength = event.signalStrength
        refreshViews()

    }

    private fun refreshViews() {
        if (isConnected) {
            tvIsConnected.text = getString(R.string.is_connected)
            tvTransportType.visibility = View.VISIBLE
            if (isWifi) {
                tvTransportType.text = getString(R.string.transport_type_wifi)
                tvSignalStrength.visibility = View.VISIBLE
                val signalText: String = getString(R.string.signal_strength).plus("$signalStrength")
                tvSignalStrength.text = signalText
            }
            else {
                tvTransportType.text = getString(R.string.transport_type_cellular)
                tvSignalStrength.visibility = View.GONE
            }

            tvLinkDnBandwidth.visibility = View.VISIBLE
            val linkDnText: String = getString(R.string.link_dn_bandwidth).plus("$linkDnBandwidth")
            tvLinkDnBandwidth.text = linkDnText
            tvLinkUpBandwidth.visibility = View.VISIBLE
            val linkUpText: String = getString(R.string.link_up_bandwidth).plus("$linkUpBandwidth")
            tvLinkUpBandwidth.text = linkUpText
        }
        else {
            tvIsConnected.text = getString(R.string.is_connected_no)
            tvTransportType.visibility = View.GONE
            tvLinkDnBandwidth.visibility = View.GONE
            tvLinkUpBandwidth.visibility = View.GONE
            tvSignalStrength.visibility = View.GONE
        }

    }

    private fun registerForNetworkEvent() {
        if (!EventBus.getDefault().hasSubscriberForEvent(NetworkState::class.java)) {
            EventBus.getDefault().register(this@MainActivity)
        }
    }

    private fun unregisterForNetworkEvent() {
        EventBus.getDefault().unregister(this@MainActivity)
    }
}