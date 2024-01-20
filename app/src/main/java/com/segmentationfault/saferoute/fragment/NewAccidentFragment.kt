package com.segmentationfault.saferoute.fragment

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentNewAccidentBinding
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
import java.time.LocalDateTime

class NewAccidentFragment : Fragment(R.layout.fragment_new_accident) {
    private lateinit var binding: FragmentNewAccidentBinding
    private lateinit var app: MyApplication

    private var isImageFitToScreen = false
    private lateinit var imageViewLayout: ViewGroup.LayoutParams

    private var imageBase64 = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewAccidentBinding.bind(view)
        app = requireContext().applicationContext as MyApplication

        binding.openCaptureButton.setOnClickListener {
            findNavController().navigate(R.id.action_newAccidentFragment_to_captureFragment)
        }

        imageViewLayout = binding.captureImage.layoutParams

        binding.captureImage.setOnClickListener {
            if (isImageFitToScreen) {
                isImageFitToScreen = false
                binding.captureImage.layoutParams = imageViewLayout
                binding.captureImage.adjustViewBounds = true
            } else {
                isImageFitToScreen = true
                binding.captureImage.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams
                        .MATCH_PARENT
                )
                binding.captureImage.scaleType = ImageView.ScaleType.FIT_XY
            }
        }

        setFragmentResultListener("resultFromCapture") { _, bundle ->
            val photoUri = bundle.getString("photoUri").toString()
            val uri = Uri.parse(photoUri)

            binding.captureImage.setImageURI(uri)
            binding.captureImage.rotation = 90f

            requestDataFromApi(uri)
        }

        binding.submitButton.setOnClickListener {
            sendAccidentRequestToApi()
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.accident_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
        }

        if (app.username == "") {
            Toast.makeText(requireContext(), "Log in to submit accidents!", Toast.LENGTH_LONG).show()
            binding.submitButton.isEnabled = false
            binding.openCaptureButton.isEnabled = false
            binding.spinner.isEnabled = false
            binding.descriptionInput.isEnabled = false
        }
    }

    private fun requestDataFromApi(photoUri: Uri) {
        binding.statusText.text = "Analyzing..."

        val bytes: ByteArray
        requireActivity().contentResolver.openInputStream(photoUri).use { inputStream ->
            bytes = inputStream!!.readBytes()
        }
        imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)

        val payload = JSONObject()
        payload.put("photo", imageBase64)

        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = payload.toString().toRequestBody(json)

        val request = Request.Builder()
            .post(requestBody)
            .url(MyApplication.RECOGNITION_API)
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.statusText.text = "Analysis failed..."
                }

                println(e.message)
                println(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 201)
                    return

                val jsonObject = JSONObject(response.body!!.string())
                val decodedString: ByteArray = Base64.decode(jsonObject.getString("result"), Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                requireActivity().runOnUiThread {
                    binding.statusText.text = "Detection finished. Found " + jsonObject.getInt("num") + " car/s."
                    binding.captureImage.setImageBitmap(decodedByte)
                    binding.captureImage.rotation = 0f
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendAccidentRequestToApi() {
        if (imageBase64.isEmpty() || binding.descriptionInput.text.isEmpty()) {
            requireActivity().runOnUiThread {
                binding.accidentStatus.text = "Photo and description are required!"
            }
            return
        }

        val payload = JSONObject()
        payload.put("dateTime", LocalDateTime.now().toString())
        payload.put("coordinatesX", 0)
        payload.put("coordinatesY", 0)
        payload.put("accidentType", binding.spinner.selectedItem)
        payload.put("username", app.username)
        payload.put("text", binding.descriptionInput.text)
        payload.put("photo", imageBase64)

        val json: MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = payload.toString().toRequestBody(json)

        val request = Request.Builder()
            .post(requestBody)
            .url(MyApplication.DATABASE_API + "/new-accidents")
            .build()
        app.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.accidentStatus.text = "Submission failed. Try again!"
                }
                println(e.message)
                println(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 201)
                    requireActivity().supportFragmentManager.popBackStack()
            }
        })
    }
}