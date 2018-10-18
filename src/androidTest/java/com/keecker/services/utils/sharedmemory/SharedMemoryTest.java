/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Contributors: Thomas Gallagher
 */
package com.keecker.services.utils.sharedmemory;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SharedMemoryTest {

    @Test
    public void testConstructor() {
        try {
            SharedMemory<SharedMemoryBuffer> sharedMemory = new SharedMemory<>(null, 0, 0);
            Assert.fail("Constructor should have thrown");
        } catch (Exception ignored) {}
        try {
            SharedMemory<SharedMemoryBuffer> sharedMemory = new SharedMemory<>(SharedMemoryBuffer.class, -1, 0);
            Assert.fail("Constructor should have thrown");
        } catch (Exception ignored) {}
        try {
            SharedMemory<SharedMemoryBuffer> sharedMemory = new SharedMemory<>(SharedMemoryBuffer.class, 0, -1);
            Assert.fail("Constructor should have thrown");
        } catch (Exception ignored) {}
        try {
            SharedMemory<SharedMemoryBuffer> sharedMemory = new SharedMemory<>(SharedMemoryBuffer.class, 10, 1024);
        } catch (Exception e) {
            Assert.fail("Constructor should not have thrown");
        }
    }

    @Test
    public void testEnqueueDequeueAshmem() throws InterruptedException {
        final int bufferSize = 1024;
        final SharedMemory<SharedMemoryBuffer> sharedMemory = new SharedMemory<>(SharedMemoryBuffer.class, 4, bufferSize);
        testEnqueueDequeue(sharedMemory);
        sharedMemory.destroy();
    }

    @Test
    public void testOnlyDequeue() {
        final int bufferSize = 1024;
        final int numBuffers = 4;
        final SharedMemory<SharedMemoryBuffer> sharedMemory = new SharedMemory<>(SharedMemoryBuffer.class, numBuffers, bufferSize);
        for (int i = 0; i < numBuffers; i++) {
            assertNotNull("Should have got a valid BufferInfo", sharedMemory.dequeueBuffer());
        }
        for (int i = numBuffers; i < 3 * numBuffers; i++) {
            assertNull("Should have got a null BufferInfo", sharedMemory.dequeueBuffer());
        }
        sharedMemory.destroy();
    }

    private void testEnqueueDequeue(final SharedMemory sharedMemory) throws InterruptedException {
        final int bufferSize = sharedMemory.getBufferSize();
        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    SharedMemoryBuffer buffer = sharedMemory.dequeueBuffer();
                    Assert.assertNotNull("dequeueBuffer returned null", buffer);
                    Assert.assertEquals("size don't match", bufferSize, buffer.getSize());
                    Assert.assertNotEquals("file descriptor should not be -1", -1L,
                            buffer.getPfd().getFileDescriptor());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                    sharedMemory.enqueueBuffer(buffer.getBufferId());
                }
            }
        });
        consumer.start();
        consumer.join();
    }
}
