package com.segmentationfault.saferoute.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentRegisterBinding
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

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var app: MyApplication

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        app = requireContext().applicationContext as MyApplication

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.registerButton.setOnClickListener {
            sendRegisterToApi()
        }
    }

    private fun sendRegisterToApi() {
        if (binding.passwordInput.text.toString() != binding.repeatPasswordInput.text.toString()) {
            requireActivity().runOnUiThread {
                binding.registerStatus.text = getString(R.string.register_passwords_do_not_match)
            }
            return
        }

        val payload = JSONObject()
        payload.put("email", binding.emailInput.text.toString())
        payload.put("username", binding.usernameInput.text.toString())
        payload.put("password", binding.passwordInput.text.toString())

        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = payload.toString().toRequestBody(json)

        val request = Request.Builder()
            .post(requestBody)
            .url(MyApplication.DATABASE_API + "/register")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.registerStatus.text = getString(R.string.register_failed)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 201) {
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity().runOnUiThread {
                        binding.registerStatus.text = getString(R.string.register_failed)
                    }
                }
            }
        })
    }
}
