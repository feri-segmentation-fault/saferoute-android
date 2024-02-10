package com.segmentationfault.saferoute

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.segmentationfault.saferoute.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)

        navController = navHostFragment.navController
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply {
            if (extras?.getString(NEW_ACCIDENT_INFO_KEY) == NEW_ACCIDENT_INFO_KEY_ANSWER_OPEN) {
                val bundle = Bundle().apply {
                    putDouble("latitude", extras?.getDouble("latitude") ?: 46.5547)
                    putDouble("longitude", extras?.getDouble("longitude") ?: 15.6459)
                }
                navController.navigate(R.id.accidentsFragment, bundle)
            }

            cancelAllNotification()
        }
    }

    private fun cancelAllNotification() {
        val ns = NOTIFICATION_SERVICE
        val nMgr = applicationContext.getSystemService(ns) as NotificationManager
        nMgr.cancelAll()
    }

    private fun createNotificationChannel() {
        val name = "SafeRoute"
        val descriptionText = "Contains notifications from the SafeRoute app."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotificationNewAccident(title: String, content: String, latitude: Double, longitude: Double) {
        val intentAccident = Intent(this, MainActivity::class.java)
        intentAccident.putExtra(NEW_ACCIDENT_INFO_KEY, NEW_ACCIDENT_INFO_KEY_ANSWER_OPEN)
        intentAccident.putExtra("latitude", latitude)
        intentAccident.putExtra("longitude", longitude)

        intentAccident.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntentAccident = PendingIntent.getActivity(
            this,
            getNotificationUniqueID(),
            intentAccident,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.car)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                R.drawable.earth, "Open",
                pendingIntentAccident
            )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(getNotificationUniqueID(), builder.build())
    }

    companion object {
        const val CHANNEL_ID = "com.segmentationfault.saferoute"
        const val NEW_ACCIDENT_INFO_KEY = "com.segmentationfault.saferoute.newAccident"
        const val NEW_ACCIDENT_INFO_KEY_ANSWER_OPEN = "OPEN"
        private var notificationId = 0
        fun getNotificationUniqueID(): Int {
            return notificationId++
        }
    }
}