package com.vladan.internetmonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_check_internet.setOnClickListener {
            object : Thread(){
                override fun run() {
                    val ping: ArrayList<String>? = AppUtils.executeCmd("ping -c 3 -q google.com")
                    Log.d(TAG, "Ping : $ping")
                }
            }.start()

        }
    }
}