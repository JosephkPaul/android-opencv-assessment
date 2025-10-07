package com.example.edgedetectionapp

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: MyGLRenderer
    private lateinit var processedBuffer: ByteBuffer

    // Handles the result of the permission request
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera() // Permission granted, start the camera
            } else {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize Views and Renderer first
        glSurfaceView = findViewById(R.id.glSurfaceView)
        renderer = MyGLRenderer()

        // 2. Configure the GLSurfaceView
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)

        // 3. Now, check for permissions and start the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            activityResultLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Initialize the output buffer once, based on the frame size
            imageAnalysis.resolutionInfo?.let {
                val resolution = it.resolution
                // RGBA has 4 bytes per pixel
                processedBuffer = ByteBuffer.allocateDirect(resolution.width * resolution.height * 4)
            }

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                // Ensure the buffer is initialized before using it
                if (!::processedBuffer.isInitialized) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                // Call our C++ function via the JNI bridge
                NativeLib.processFrame(
                    imageProxy.width,
                    imageProxy.height,
                    imageProxy.planes[0].buffer, // Y plane
                    imageProxy.planes[1].buffer, // U plane
                    imageProxy.planes[2].buffer, // V plane
                    imageProxy.planes[0].rowStride,
                    imageProxy.planes[1].rowStride,
                    processedBuffer
                )

                // Pass the processed buffer to the OpenGL renderer
                renderer.updateFrame(processedBuffer, imageProxy.width, imageProxy.height)

                // IMPORTANT: Close the imageProxy to receive the next frame
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

    companion object {
        // Used to load the 'edgedetectionapp' library on application startup.
        init {
            System.loadLibrary("edgedetectionapp")
        }
    }
}