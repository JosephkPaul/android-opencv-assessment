// In app/src/main/java/com/example/edgedetectionapp/NativeLib.kt

package com.example.edgedetectionapp

import java.nio.ByteBuffer

object NativeLib {

    init {
        // The name must match the library name in your CMakeLists.txt
        System.loadLibrary("edgedetectionapp")
    }

    /**
     * The native function that will be implemented in C++.
     * It takes the YUV planes from the camera, processes them,
     * and writes the RGBA result into the output buffer.
     */
    external fun processFrame(
        width: Int,
        height: Int,
        yPlane: ByteBuffer,
        uPlane: ByteBuffer,
        vPlane: ByteBuffer,
        yStride: Int,
        uvStride: Int,
        outputBuffer: ByteBuffer
    )
}