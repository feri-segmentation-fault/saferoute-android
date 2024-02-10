package com.segmentationfault.saferoute.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentAccidentsBinding
import com.segmentationfault.saferoute.models.Accident
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AccidentsFragment : Fragment(R.layout.fragment_accidents) {
    private lateinit var binding: FragmentAccidentsBinding
    private lateinit var app: MyApplication
    private val requestPermissionLocation = 1
    private lateinit var mapController: IMapController
    private lateinit var map: MapView
    private val defaultZoom = 17.5
    private var defaultLocation: GeoPoint? = GeoPoint(46.5547, 15.6459)

    private val dateFormatterFrom: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private val dateFormatterTo: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccidentsBinding.inflate(inflater, container, false)
        app = requireContext().applicationContext as MyApplication

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        )

        map = binding.map
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.setMultiTouchControls(true)
        mapController = map.controller

        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)

        requestLocationUpdates()

        getAccidentsFromApi()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup Info
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")

        if (latitude != null && longitude != null) {
            defaultLocation = GeoPoint(latitude, longitude)
            mapController.setCenter(defaultLocation)
        }
    }

    private fun getAccidentsFromApi() {
        val request = Request.Builder()
            .get()
            .url(MyApplication.DATABASE_API + "/new-accidents")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e.message)
                println(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200)
                    return

                val gson = Gson()
                val res = JSONArray(response.body!!.string())

                for (i in 0 until res.length()) {
                    val accidentJson = res.getJSONObject(i).toString()
                    val accident = gson.fromJson(accidentJson, Accident::class.java)

                    val tmpDateTime = LocalDateTime.parse(accident.dateTime, dateFormatterFrom)

                    val markerPosition = GeoPoint(accident.latitude, accident.longitude)

                    try {
                        val testMarker = Marker(map)
                    } catch (e: Exception) {
                        return
                    }

                    val marker = Marker(map)
                    marker.title = MyApplication.TRANSL_ACCIDENT_TYPE[accident.accidentType]
                    marker.title += "\n\nDescription:\n" + accident.description
                    marker.title += "\n\nReported:\n" + tmpDateTime.format(dateFormatterTo) + ", " + accident.username
                    marker.position = markerPosition
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    if (accident.photoB64 !== "") {
                        val decodedString: ByteArray = Base64.decode(accident.photoB64, Base64.DEFAULT)
                        val decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                        val matrix = Matrix()
                        matrix.postRotate(90f)
                        val rotatedBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)

                        try {
                            val testMarker = Marker(map)
                        } catch (e: Exception) {
                            return
                        }

                        marker.image = rotatedBitmap.toDrawable(resources)
                    }

                    map.overlays.add(marker)
                }
            }
        })
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider: String = LocationManager.GPS_PROVIDER
            val lastKnownLocation: Location? = locationManager.getLastKnownLocation(locationProvider)

            if (lastKnownLocation != null) {
                val startPoint = GeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude)
                mapController.setCenter(startPoint)
                mapController.setZoom(defaultZoom)
            } else {
                mapController.setCenter(defaultLocation)
                mapController.setZoom(defaultZoom)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestPermissionLocation
            )
            mapController.setCenter(defaultLocation)
            mapController.setZoom(defaultZoom)
        }
    }
}