package com.segmentationfault.saferoute

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        const val SHAREDPREF_FILE = "com.segmentationfault.saferoute_preferences"
        const val MY_SHAREDPREF_FILE = "mySharedData"
    }
}