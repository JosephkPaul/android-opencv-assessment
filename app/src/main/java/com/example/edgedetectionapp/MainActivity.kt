package com.example.edgedetectionapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {




    // Handles the result of the permission request
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera() // Permission granted, start the camera
            } else {
                // Handle the case where the user denies the permission
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            // Request permission
            activityResultLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Select the back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Set up the ImageAnalysis use case to get a stream of frames [cite: 25]
            val imageAnalysis = ImageAnalysis.Builder()
                // Use the latest frame and discard old ones if processing is slow
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // This is the core of this step.
            // setAnalyzer provides a callback that receives a frame (ImageProxy) for processing.
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                // THIS IS WHERE YOU WILL GET EACH CAMERA FRAME!
                // In later steps, you will pass this imageProxy to your native code.
                // For now, it's crucial to close it to get the next frame.
                Log.d("CameraX", "Frame received: ${imageProxy.width}x${imageProxy.height}")
                imageProxy.close()
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the camera selector and imageAnalysis use case to the activity's lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }



    /**
     * A native method that is implemented by the 'edgedetectionapp' native library,
     * which is packaged with this application.
     */

    companion object {
        // Used to load the 'edgedetectionapp' library on application startup.
        init {
            System.loadLibrary("edgedetectionapp")
        }
    }
}