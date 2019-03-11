package com.keecker.embedded.camera.memory

import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileDescriptor
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicInteger

class KeeckerSharedMemory(private val fileDescriptor: FileDescriptor, private val length: Int) {

    constructor(fd: ParcelFileDescriptor, length: Int) : this(fd.fileDescriptor, length)

    private val refCounter = AtomicInteger()
    private val fileInputStream = FileInputStream(fileDescriptor)
    private var bufferData: ByteArray? = null

    fun getBytes(): ByteArray? {
        val clientIndex = refCounter.incrementAndGet()
        if (clientIndex == 1) {
            try {
                val mappedByteBuffer = fileInputStream.channel.
                        map(
                                FileChannel.MapMode.READ_ONLY,
                                0,
                                length.toLong())
                if (!mappedByteBuffer.hasArray()) {
                    bufferData = ByteArray(length)
                    mappedByteBuffer.get(bufferData)
                } else {
                    bufferData = mappedByteBuffer.array()
                }
            } catch (e: Exception) {
                Log.e("SharedMemory",
                        "Unable to fetch ($length) bytes from file descriptor $fileDescriptor",
                        e)
            }
        }
        return bufferData
    }

    fun release() {
        val clientIndex = refCounter.getAndDecrement()
        if (clientIndex == 1) {
            bufferData = null
            fileInputStream.close()
        }
    }

    private fun finalize() {
        if (refCounter.get() > 0 || bufferData != null) {
            Log.e("SharedMemory",
                    "Shared memory has not been released..." +
                    "Releasing it now!")
            release()
        }
    }
}
