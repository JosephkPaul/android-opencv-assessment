#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <cstdint>

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgedetectionapp_NativeLib_processFrame(
        JNIEnv *env,
        jobject, /* this */
        jint width,
        jint height,
        jobject y_plane,
        jobject u_plane,
        jobject v_plane,
        jint y_stride,
        jint uv_stride,
        jobject output_buffer) {

    // 1. Get direct access to the raw pixel data from the ByteBuffers
    auto y_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(y_plane));
    auto u_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(u_plane));
    // The v_plane is often interleaved with the u_plane in many formats.
    // We will primarily use y_data and u_data for a common conversion.
    auto output_data = static_cast<uint8_t *>(env->GetDirectBufferAddress(output_buffer));

    // 2. Create cv::Mat headers that wrap the YUV data without copying it
    // The Y plane is a single channel (grayscale) image
    cv::Mat y_mat(height, width, CV_8UC1, y_data, y_stride);
    // The UV plane is typically semi-planar and interleaved (2 channels)
    cv::Mat uv_mat(height / 2, width / 2, CV_8UC2, u_data, uv_stride);

    // 3. Convert the YUV image to RGBA
    // We use COLOR_YUV2RGBA_NV21, a common format for Android camera frames.
    cv::Mat rgba_mat;
    cv::cvtColorTwoPlane(y_mat, uv_mat, rgba_mat, cv::COLOR_YUV2RGBA_NV21);

    // --- APPLY OPENCV FILTER ---
    // First, convert the color image to grayscale
    cv::Mat gray_mat;
    cv::cvtColor(rgba_mat, gray_mat, cv::COLOR_RGBA2GRAY);

    // Second, apply the Canny edge detection algorithm
    cv::Mat canny_edges;
    // The threshold values (50, 150) are common starting points
    cv::Canny(gray_mat, canny_edges, 50.0, 150.0);

    // --- PREPARE OUTPUT ---
    // Convert the single-channel Canny output back to a 4-channel RGBA image for rendering
    cv::Mat output_mat;
    cv::cvtColor(canny_edges, output_mat, cv::COLOR_GRAY2RGBA);

    // 4. Copy the processed RGBA data into the output buffer that was passed from Kotlin
    // The size is width * height * 4 because RGBA has 4 channels (4 bytes per pixel)
    memcpy(output_data, output_mat.data, width * height * 4);
}