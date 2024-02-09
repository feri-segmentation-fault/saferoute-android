package com.segmentationfault.saferoute.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentLoginBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var app: MyApplication

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        app = requireContext().applicationContext as MyApplication

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.loginButton.setOnClickListener {
            sendLoginToApi()
        }

        binding.openRegisterButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun sendLoginToApi() {
        val payload = JSONObject()
        payload.put("username", binding.usernameInput.text.toString())
        payload.put("password", binding.passwordInput.text.toString())

        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = payload.toString().toRequestBody(json)

        val request = Request.Builder()
            .post(requestBody)
            .url(MyApplication.DATABASE_API + "/login")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.loginStatus.text = getString(R.string.login_status_failed)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    app.cookie = response.headers("Set-Cookie")[0]
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity().runOnUiThread {
                        binding.loginStatus.text = getString(R.string.login_status_failed)
                    }
                }
            }
        })
    }
}
