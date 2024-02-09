package com.segmentationfault.saferoute.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.SetRecyclerViewAdapter
import com.segmentationfault.saferoute.databinding.FragmentAccelerationListBinding
import com.segmentationfault.saferoute.models.Acceleration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type

class AccelerationList : Fragment() {
    private lateinit var _binding: FragmentAccelerationListBinding
    private lateinit var adapter: SetRecyclerViewAdapter
    private lateinit var app: MyApplication

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccelerationListBinding.inflate(inflater, container, false)
        adapter = SetRecyclerViewAdapter(requireContext(), mutableListOf())
        app = MyApplication()

        val gson = Gson()

        val recyclerView: RecyclerView = _binding.mainView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val request = Request.Builder()
            .get()
            .url(MyApplication.DATABASE_API + "/acceleration")
            .addHeader("Cookie", app.cookie)
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val accelerationListType: Type = object : TypeToken<List<Acceleration>>() {}.type
                val accelerationList: List<Acceleration> = gson.fromJson(response.body?.string(), accelerationListType)
                activity!!.runOnUiThread {
                    adapter.updateAccelerations(accelerationList)
                }
            }
        })

        return _binding.root
    }
}