/**
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

#include <jni.h>
#include <jni_utils/log.h>

extern "C" {

JNIEXPORT jbyteArray JNICALL Java_com_keecker_embedded_camera_memory_KeeckerSharedMemory_getBytesFromAddress(
        JNIEnv *env, jobject thiz, jlong addr, jsize length) {
    jbyteArray tt = env->NewByteArray(length);
    if (tt == nullptr) {
        ALOGE("KeeckerSharedMemory JNI", "Unable to allocate byte array of size %d", length);
        return nullptr;
    }
    env->SetByteArrayRegion(tt, 0, length, reinterpret_cast<jbyte *>(addr));
    return tt;
}

}   // extern C
