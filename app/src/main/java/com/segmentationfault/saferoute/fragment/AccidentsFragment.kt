package com.segmentationfault.saferoute.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentAccidentsBinding
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
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException

class AccidentsFragment : Fragment(R.layout.fragment_accidents) {
    private lateinit var binding: FragmentAccidentsBinding
    private lateinit var app: MyApplication
    private val requestPermissionLocation = 1
    private lateinit var mapController: IMapController
    private lateinit var map: MapView
    private val defaultZoom = 17.5
    private var defaultLocation: GeoPoint? = GeoPoint(46.5547, 15.6459)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccidentsBinding.inflate(inflater, container, false)
        app = requireContext().applicationContext as MyApplication

        // getAccidentsFromApi()

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

        return binding.root
    }

    private fun getAccidentsFromApi() {
        // binding.statusText.text = "Retrieving accidents..."

        val request = Request.Builder()
            .get()
            .url(MyApplication.DATABASE_API + "/new-accidents")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    // binding.statusText.text = "Retrieving failed."
                }

                println(e.message)
                println(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    requireActivity().runOnUiThread {
                        // binding.statusText.text = "Retrieving failed."
                    }
                    return
                }

                requireActivity().runOnUiThread {
                    // binding.statusText.text = "Retrieving success."
                }

                val jsonArray = JSONArray(response.body!!.string())
            }
        })
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider: String = LocationManager.GPS_PROVIDER
            val lastKnownLocation: Location? =
                locationManager.getLastKnownLocation(locationProvider)

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