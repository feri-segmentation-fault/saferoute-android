package com.segmentationfault.saferoute.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.MySharedPreferences
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    private lateinit var app: MyApplication

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        app = requireContext().applicationContext as MyApplication

        binding.openLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }

        binding.logoutButton.setOnClickListener {
            sendLogoutRequest()
        }

        getCurrentUser()

        val submitLimitBtn = binding.submitLimitBtn
        val viewAccelerationsBtn = binding.viewAccelerationsButton
        val limitInput = binding.limitInput

        val prefs = MySharedPreferences(requireContext())
        val accelerationLimit = prefs.getFloat("acclmt", 20f)

        limitInput.setText(accelerationLimit.toString())

        submitLimitBtn.setOnClickListener {
            prefs.saveFloat("acclmt", limitInput.text.toString().toFloat())
            limitInput.setText(limitInput.text.toString())
        }

        viewAccelerationsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_accelerationListFragment)
        }

    }

    private fun getCurrentUser() {
        val request = Request.Builder()
            .get()
            .addHeader("Cookie", app.cookie)
            .url(MyApplication.DATABASE_API + "/current")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                app.username = ""
                updateToolbar()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val jsonObject = JSONObject(response.body!!.string())
                    app.username = jsonObject.getString("username")
                } else {
                    app.username = ""
                }
                updateToolbar()
            }
        })
    }

    private fun updateToolbar() {
        requireActivity().runOnUiThread {
            if (app.username == "") {
                binding.welcomeText.text = "Welcome!"
                binding.logoutButton.visibility = View.INVISIBLE
                binding.openLoginButton.visibility = View.VISIBLE
            } else {
                binding.welcomeText.text = "Welcome, " + app.username + "!"
                binding.logoutButton.visibility = View.VISIBLE
                binding.openLoginButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun sendLogoutRequest() {
        val request = Request.Builder()
            .get()
            .url(MyApplication.DATABASE_API + "/logout")
            .addHeader("Cookie", app.cookie)
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.headers("Set-Cookie")[0].split(";")[0] == "token=")
                    app.cookie = ""

                getCurrentUser()
            }
        })
    }
}