package com.example.edgedetectionapp

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Called once to set up the view's OpenGL ES environment.
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f) // Red, Green, Blue, Alpha
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Called if the geometry of the view changes.
        GLES20.glViewport(0, 0, width, height)
    }



    override fun onDrawFrame(gl: GL10?) {
        // Called for each redraw of the view.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}