package com.segmentationfault.saferoute

import android.app.Application
import okhttp3.OkHttpClient

class MyApplication : Application() {
    val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        const val SHAREDPREF_FILE = "com.segmentationfault.saferoute_preferences"
        const val MY_SHAREDPREF_FILE = "mySharedData"

        const val RECOGNITION_API = "http://192.168.1.5:5000/detect"
        const val DATABASE_API = "http://192.168.1.5:3080"
    }
}