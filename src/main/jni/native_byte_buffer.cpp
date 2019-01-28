/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Thomas Gallagher
 */

#include <jni.h>
#include <sys/mman.h>
#include <string.h>
#include <errno.h>
#include <string.h>
#include <android/log.h>

extern "C" {

JNIEXPORT jobject JNICALL Java_com_keecker_services_interfaces_utils_sharedmemory_NativeByteBuffer_fdToByteBuffer(
    JNIEnv * env, jobject /*thiz*/, jint fd, jint size) {
    void * ptr = mmap(0, size, PROT_READ, MAP_SHARED, static_cast<int>(fd), 0);
    if (ptr == MAP_FAILED) {
        __android_log_print(ANDROID_LOG_ERROR, "JniNativeByteBuffer",
                            "fdByteToBuffer failed to mmap on fd %d error %d", fd, errno);
        return NULL;
    }
    // TODO(thomas): check mmap failures and handle errors somehow
    jobject bb = env->NewDirectByteBuffer(reinterpret_cast<void*>(ptr), size);
    if (bb == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "JniNativeByteBuffer",
            "NewDirectByteBuffer failed");
        return NULL;
    }
    return bb;
}

JNIEXPORT jint JNICALL Java_com_keecker_services_interfaces_utils_sharedmemory_NativeByteBuffer_munmapByteBuffer(
    JNIEnv * env, jobject /*thiz*/, jobject byte_buffer, jint size) {
    void * addr = env->GetDirectBufferAddress(byte_buffer);
    int err = munmap(addr, size);
    return err;
}

JNIEXPORT void JNICALL Java_com_keecker_services_interfaces_utils_sharedmemory_NativeByteBuffer_copyByteArrayToFd(
    JNIEnv *env, jobject /*thiz*/, jbyteArray src, jint destFd)
{
    jsize array_length = env->GetArrayLength(src);
    jbyte* buffer_ptr = env->GetByteArrayElements(src, NULL);
    // mmap to get a pointer from the file descriptor
    void *dst_ptr = mmap(0, array_length, PROT_READ | PROT_WRITE, MAP_SHARED, static_cast<int>(destFd), 0);
    if (dst_ptr == MAP_FAILED) {
        __android_log_print(ANDROID_LOG_ERROR, "JniNativeByteBuffer",
                            "map (size = %d, fd = %d) failed with errno = %d", array_length, destFd, errno);
        return;
    } else {
        memcpy(dst_ptr, buffer_ptr, array_length);
    }
    munmap(dst_ptr, array_length);
    env->ReleaseByteArrayElements(src, buffer_ptr, 0);
}

}   // extern C
