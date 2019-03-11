/**
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

#include <jni.h>
#include <android/log.h>
#include <cstddef>

extern "C" {

JNIEXPORT jbyteArray JNICALL Java_com_keecker_embedded_camera_memory_KeeckerSharedMemory_getBytesFromAddress(
        JNIEnv *env, jobject, jlong addr, jsize length) {
    jbyteArray tt = env->NewByteArray(length);
    if (tt == NULL) {
        __android_log_write(ANDROID_LOG_ERROR, "KeeckerSharedMemory JNI", "Unable to allocate byte array");
        return NULL;
    }
    env->SetByteArrayRegion(tt, 0, length, reinterpret_cast<jbyte *>(addr));
    return tt;
}

}   // extern C
