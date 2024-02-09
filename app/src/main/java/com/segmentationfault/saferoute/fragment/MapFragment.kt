package com.segmentationfault.saferoute.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentMapBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


class MapFragment : Fragment(R.layout.fragment_map) {

    private lateinit var binding: FragmentMapBinding
    private lateinit var app: MyApplication
    private lateinit var mapController: IMapController
    private lateinit var map: MapView
    private val defaultZoom = 17.5
    private var defaultLocation: GeoPoint? = GeoPoint(46.5547, 15.6459)
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        app = requireContext().applicationContext as MyApplication

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()

        binding.selectXYButton.setOnClickListener {
            locationSelected()
        }
    }

    private fun initMap() {
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        )

        map = binding.mapSelectXY
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.setMultiTouchControls(true)
        mapController = map.controller

        // Handle Map Clicks
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    placeMarker(p)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Handle long press on the map if needed
                return false
            }
        })

        // Set default location
        mapController.setCenter(defaultLocation)
        mapController.setZoom(defaultZoom)
        map.overlays.add(mapEventsOverlay)
    }

    private fun placeMarker(geoPoint: GeoPoint) {
        if (marker == null) {
            marker = Marker(map)
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.title = getString(R.string.map_marker_title)
            map.overlays.add(marker)
        }
        marker!!.position = geoPoint

        map.invalidate()
    }

    private fun locationSelected() {
        val selectedMarker = marker

        if (selectedMarker != null) {
            val latitude = selectedMarker.position.latitude
            val longitude = selectedMarker.position.longitude

            val resultBundle = bundleOf("latitude" to latitude, "longitude" to longitude)

            setFragmentResult("resultFromMapXY", resultBundle)
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), getString(R.string.map_no_location_selected), Toast.LENGTH_SHORT).show()
        }
    }
}