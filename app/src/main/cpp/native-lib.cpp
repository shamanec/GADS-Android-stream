#include <jni.h>
#include <turbojpeg.h>

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_shamanec_stream_ScreenCaptureService_compressImageToJpeg(JNIEnv *env, jobject, jobject bitmap, jint quality) {
    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return nullptr;
    }

    void *pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    tjhandle jpegCompressor = tjInitCompress();

    unsigned char *jpegBuffer = nullptr;
    unsigned long jpegSize = 0;

    int result = tjCompress2(jpegCompressor, static_cast<unsigned char *>(pixels),
                             bitmapInfo.width, bitmapInfo.stride, bitmapInfo.height,
                             TJPF_RGBA, &jpegBuffer, &jpegSize, TJSAMP_444, quality, 0);

    AndroidBitmap_unlockPixels(env, bitmap);

    tjDestroy(jpegCompressor);

    if (result != 0) {
        return nullptr;
    }

    jbyteArray jpegArray = env->NewByteArray(jpegSize);
    env->SetByteArrayRegion(jpegArray, 0, jpegSize, reinterpret_cast<jbyte *>(jpegBuffer));

    tjFree(jpegBuffer);

    return jpegArray;
}