package com.segmentationfault.saferoute.fragment

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.segmentationfault.saferoute.MyApplication
import com.segmentationfault.saferoute.R
import com.segmentationfault.saferoute.databinding.FragmentCaptureBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureFragment: Fragment(R.layout.fragment_capture) {
    private lateinit var binding: FragmentCaptureBinding
    private lateinit var app: MyApplication

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCaptureBinding.bind(view)
        app = requireContext().applicationContext as MyApplication

        if (isCameraPermissionGranted())
            startCamera()
        else
            requestCameraPermission()

        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        if (!isCameraPermissionGranted()) {
            Toast.makeText(requireContext(), "No camera permission", Toast.LENGTH_SHORT).show()
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("dd-MM-yyyy hh-mm-ss", Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val data = bundleOf()
                    data.putString("photoUri", Uri.fromFile(photoFile).toString())

                    setFragmentResult("resultFromCapture", data)
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            )
            { isGranted ->
                if (!isGranted)
                    Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_SHORT).show()

                startCamera()
            }

        activityResultLauncher.launch(android.Manifest.permission.CAMERA)
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}