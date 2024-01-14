package com.segmentationfault.saferoute

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.segmentationfault.saferoute.databinding.ActivityMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val url = "http://192.168.1.5:5000/detect"
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraButton.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            getPhotoFromCamera.launch(intent)
        }
    }

    private val getPhotoFromCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoUri = result.data!!.getStringExtra("photoUri").toString()
            val uri = Uri.parse(photoUri)

            binding.captureImage.setImageURI(uri)
            binding.captureImage.rotation = 90f

            requestDataFromApi(uri)
        }
    }

    private fun requestDataFromApi(photoUri: Uri) {
        val bytes: ByteArray
        contentResolver.openInputStream(photoUri).use { inputStream ->
            bytes = inputStream!!.readBytes()
        }
        val imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)

        val payload = JSONObject()
        payload.put("photo", imageBase64)

        val json : MediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = payload.toString().toRequestBody(json)

        val request = Request.Builder()
            .post(requestBody)
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed")
                println(e.message)
                println(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                println("Request success")

                if (response.code != 201)
                    return

                val jsonObject = JSONObject(response.body!!.string())
                val decodedString: ByteArray = Base64.decode(jsonObject.getString("result"), Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                runOnUiThread {
                    binding.captureImage.setImageBitmap(decodedByte)
                }
            }
        })
    }
}