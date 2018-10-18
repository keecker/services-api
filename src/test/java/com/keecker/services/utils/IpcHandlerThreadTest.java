/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Queue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IpcHandlerThreadTest {

    private enum Fruit {
        BANANA, LEMON
    }

    private IpcHandlerThread.Delegate<Fruit> mDelegate;
    private IpcHandlerThread<Fruit> mIpcHandlerThread;

    @Before
    public void before() {
        mDelegate  = (IpcHandlerThread.Delegate<Fruit>) mock(IpcHandlerThread.Delegate.class);
        mIpcHandlerThread = new IpcHandlerThread<>(Fruit.class, 10, mDelegate, null);
    }

    @After
    public void after() {
        if (mIpcHandlerThread != null) {
            mIpcHandlerThread.interrupt();
        }
    }

    @Test
    public void testDoesNotExit() throws InterruptedException {
        mIpcHandlerThread.start();
        Thread.sleep(500);
        assertTrue(mIpcHandlerThread.isAlive());
        mIpcHandlerThread.interrupt();
        mIpcHandlerThread.join(500);
        assertFalse(mIpcHandlerThread.isAlive());
    }

    @Test
    public void testThreadExit() throws InterruptedException {
        mIpcHandlerThread.start();
        mIpcHandlerThread.interrupt();
        mIpcHandlerThread.join(500);
        assertFalse(mIpcHandlerThread.isAlive());
        ArgumentCaptor<Queue> argumentCaptor = ArgumentCaptor.forClass(Queue.class);
        verify(mDelegate).onThreadExit(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().isEmpty());
    }

    @Test
    public void testThreadExitWithQueuedMessages() throws InterruptedException {
        mDelegate  = (IpcHandlerThread.Delegate<Fruit>) mock(IpcHandlerThread.Delegate.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(Long.MAX_VALUE);
                return null;
            }
        }).when(mDelegate).onNewMessage(any(Fruit.class));
        mIpcHandlerThread = new IpcHandlerThread<>(Fruit.class, 10, mDelegate, "");

        mIpcHandlerThread.start();
        // Queue 10 BANANAS
        for (int i = 0; i < 10; i++) {
            mIpcHandlerThread.queue(Fruit.BANANA);
        }
        // Wait for 1 BANANA to be delivered
        verify(mDelegate, timeout(500).times(1)).onNewMessage(any(Fruit.class));
        mIpcHandlerThread.interrupt();
        mIpcHandlerThread.join(500);
        assertFalse(mIpcHandlerThread.isAlive());
        ArgumentCaptor<Queue> argumentCaptor = ArgumentCaptor.forClass(Queue.class);
        verify(mDelegate).onThreadExit(argumentCaptor.capture());
        // There should be 9 bananas left
        for (int i = 0; i < 9; i++) {
            assertEquals(Fruit.BANANA, argumentCaptor.getValue().poll());
        }
        assertTrue(argumentCaptor.getValue().isEmpty());
    }

    @Test
    public void testMessagesGetDelivered() {
        mIpcHandlerThread.start();
        for (int i = 0; i < 5; i++) {
            mIpcHandlerThread.queue(Fruit.BANANA);
            verify(mDelegate, timeout(200).times(i + 1)).onNewMessage(Fruit.BANANA);
        }
        for (int i = 0; i < 5; i++) {
            mIpcHandlerThread.queue(Fruit.LEMON);
            verify(mDelegate, timeout(200).times(i + 1)).onNewMessage(Fruit.LEMON);
        }
        verify(mDelegate, times(0)).onMessageDiscarded(any(Fruit.class));
    }

    @Test
    public void testMessagesGetDiscarded() {
        mDelegate  = (IpcHandlerThread.Delegate<Fruit>) mock(IpcHandlerThread.Delegate.class);
        mIpcHandlerThread = new IpcHandlerThread<>(Fruit.class, 1, mDelegate, "");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(Long.MAX_VALUE);
                return null;
            }
        }).when(mDelegate).onNewMessage(any(Fruit.class));

        mIpcHandlerThread.start();
        // First message will get delivered and block the thread
        mIpcHandlerThread.queue(Fruit.BANANA);
        verify(mDelegate, timeout(1000).times(1)).onNewMessage(Fruit.BANANA);
        // Second message will go in the queue but never be taken out
        mIpcHandlerThread.queue(Fruit.BANANA);
        verify(mDelegate, times(1)).onNewMessage(Fruit.BANANA);
        // Subsequent messages will get discarded
        for (int i = 0; i < 10; i++) {
            mIpcHandlerThread.queue(Fruit.BANANA);
            verify(mDelegate, timeout(200).times(1)).onNewMessage(Fruit.BANANA);
            verify(mDelegate, times(i + 1)).onMessageDiscarded(Fruit.BANANA);
        }
    }
}
