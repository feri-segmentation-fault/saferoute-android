package com.segmentationfault.saferoute

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import okhttp3.OkHttpClient

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.nio.ByteBuffer

class MyApplication : Application(), SensorEventListener {
    val client = OkHttpClient()
    var cookie = ""
    var username = ""

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var x: Float = 0f
    private var y: Float = 0f

    override fun onCreate() {
        super.onCreate()

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelerometer != null) {
                sensorManager?.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } else {
                // Handle case when accelerometer sensor is not available
            }
        } else {
            // Handle case when accelerometer sensor is not supported
        }
    }

    companion object {
        const val SHAREDPREF_FILE = "com.segmentationfault.saferoute_preferences"
        const val MY_SHAREDPREF_FILE = "mySharedData"

        // AMADEJ
        const val RECOGNITION_API = "http://192.168.1.5:5000/detect"
        const val DATABASE_API = "http://192.168.1.5:3080"
        const val BLOCKCHAIN_API = "http://192.168.1.5:7234/api/Blockchain"

        // MOK
//        const val DATABASE_API = "http://192.168.1.10:3080"

        const val MQTT_BROKER_URL: String = "ssl://fb18f775ad6a416da588f836c40332b4.s2.eu.hivemq.cloud:8883"
        const val MQTT_USERNAME = "hivemq.webclient.1705871129963"
        const val MQTT_PASSWORD = "04A13FCBDSvwcz<fq,.?"
        const val CLIENT_ID: String = "android-app"
    }

    private fun mqttSendMessage(topic: String, messageInput: String) {
        try {
            val persistance = MemoryPersistence()
            val client = MqttClient(MQTT_BROKER_URL, CLIENT_ID, persistance).apply {
                connect(MqttConnectOptions().apply {
                    userName = MQTT_USERNAME
                    password = MQTT_PASSWORD.toCharArray()
                })
            }
            val message = MqttMessage(messageInput.toByteArray()).apply {
                qos = 0
            }

            client.publish(topic, message)
            client.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (username != "") {
            val prefs = MySharedPreferences(this)
            val accelerationLimit = prefs.getFloat("acclmt", 20f)
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                x = event.values[0]
                y = event.values[1]
                if (x > accelerationLimit) {
                    mqttSendMessage("acceleration", x.toString())
                }

                if (y > accelerationLimit) {
                    mqttSendMessage("acceleration", "$y;$username")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {

    }

    override fun onTerminate() {
        super.onTerminate()
        sensorManager?.unregisterListener(this)
    }
}