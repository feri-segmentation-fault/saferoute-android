package com.segmentationfault.saferoute

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.segmentationfault.saferoute.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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

            binding.captureImage.setImageURI(Uri.parse(photoUri))
            binding.captureImage.rotation = 90f
        }
    }
}