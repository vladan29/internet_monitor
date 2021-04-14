package com.vladan.internetmonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vladan.internetchecker.NetworkLiveData
import com.vladan.internetchecker.NetworkState
import com.vladan.internetmonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var isConnected: Boolean = false
    private var isWifi: Boolean = false
    private var isCellular: Boolean = false
    private var linkDnBandwidth: Int = -1
    private var linkUpBandwidth: Int = -1
    private var signalStrength: Int = 1000
    private val mNetworkLiveData : NetworkLiveData = NetworkLiveData.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }


    override fun onStart() {
        super.onStart()
        mNetworkLiveData.observe(this, Observer {
            setValues(it)
        })

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
            binding.tvIsConnected.text = getString(R.string.is_connected)
            binding.tvTransportType.visibility = View.VISIBLE
            if (isWifi) {
                binding.tvTransportType.text = getString(R.string.transport_type_wifi)
                binding.tvSignalStrength.visibility = View.VISIBLE
                val signalText: String = getString(R.string.signal_strength).plus("$signalStrength")
                binding.tvSignalStrength.text = signalText
            }
            else {
                binding.tvTransportType.text = getString(R.string.transport_type_cellular)
                binding.tvSignalStrength.visibility = View.GONE
            }

            binding.tvLinkDnBandwidth.visibility = View.VISIBLE
            val linkDnText: String = getString(R.string.link_dn_bandwidth).plus("$linkDnBandwidth")
            binding.tvLinkDnBandwidth.text = linkDnText
            binding.tvLinkUpBandwidth.visibility = View.VISIBLE
            val linkUpText: String = getString(R.string.link_up_bandwidth).plus("$linkUpBandwidth")
            binding.tvLinkUpBandwidth.text = linkUpText
        }
        else {
            binding.tvIsConnected.text = getString(R.string.is_connected_no)
            binding.tvTransportType.visibility = View.GONE
            binding.tvLinkDnBandwidth.visibility = View.GONE
            binding.tvLinkUpBandwidth.visibility = View.GONE
            binding.tvSignalStrength.visibility = View.GONE
        }

    }

}