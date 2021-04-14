package com.vladan.internetmonitor

import android.app.Application
import android.content.Context
import com.vladan.internetchecker.InternetManager


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


    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        val internetManager = InternetManager.getInternetManager(this)
        internetManager.registerInternetMonitor()
    }


}