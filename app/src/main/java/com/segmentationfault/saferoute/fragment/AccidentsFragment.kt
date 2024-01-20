package com.segmentationfault.saferoute.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentAccidentsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class AccidentsFragment : Fragment(R.layout.fragment_accidents) {
    private lateinit var binding: FragmentAccidentsBinding
    private lateinit var app: MyApplication

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAccidentsBinding.bind(view)
        app = requireContext().applicationContext as MyApplication
        
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> findNavController().navigate(R.id.action_accidentsFragment_to_mainFragment3)
                R.id.newAccident -> findNavController().navigate(R.id.action_accidentsFragment_to_newAccidentFragment)
//                R.id.accidents -> setCurrentFragment(thirdFragment)
            }
            true
        }

        getAccidentsFromApi()
    }

    private fun getAccidentsFromApi() {
        binding.statusText.text = "Retrieving accidents..."

        val request = Request.Builder()
            .get()
            .url(MyApplication.DATABASE_API + "/new-accidents")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.statusText.text = "Retrieving failed."
                }

                println(e.message)
                println(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    requireActivity().runOnUiThread {
                        binding.statusText.text = "Retrieving failed."
                    }
                    return
                }

                requireActivity().runOnUiThread {
                    binding.statusText.text = "Retrieving success."
                }

                val jsonArray = JSONArray(response.body!!.string())
            }
        })
    }
}