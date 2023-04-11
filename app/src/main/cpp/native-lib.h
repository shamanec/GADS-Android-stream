#ifndef NATIVE_LIB_H
#define NATIVE_LIB_H

#include <jni.h>

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_shamanec_stream_ScreenCaptureService_compressImageToJpeg(JNIEnv *env, jobject, jobject bitmap, jint quality);

#endif //NATIVE_LIB_H
