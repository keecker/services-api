/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.interfaces.utils;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread with a fixed size message queue that notifies a delegate whenever a new message is available.
 * If a new message arrives and the queue is full, the new message replaces the one at the top of
 * the queue.
 * @param <T> - the message type this queue deals with
 */
public class IpcHandlerThread<T> extends Thread {

    private static final String TAG = IpcHandlerThread.class.getSimpleName();

    /**
     * Callbacks when messages are available or discarded
     * @param <T> - message type
     */
    public interface Delegate<T> {
        /**
         * Called when a new message is available
         * @param msg
         */
        void onNewMessage(T msg);

        /**
         * Called when this handler had to discard a msg because the queue was full
         * @param msg
         */
        void onMessageDiscarded(T msg);

        /**
         * Called when this thread exists
         * @param remainingMsg - the messages remaining in the queue
         */
        void onThreadExit(Queue<T> remainingMsg);
    }

    private final int mQueueSize;
    private final LinkedBlockingQueue<T> mQueue = new LinkedBlockingQueue<>();
    private final Delegate<T> mDelegate;
    private final MessageStats mMessageStats = new MessageStats();
    private final String mSubscriberName;
    private final String mClassName;

    /**
     * Constructs a new handler thread, does not start it!
     * @param msgClass - the type of message
     * @param queueSize - the queue size
     * @param delegate - the delegate that will receive events
     * @param subscriberName - the name of this subscriber, can be null
     */
    public IpcHandlerThread(Class<T> msgClass, int queueSize, Delegate<T> delegate, String subscriberName) {
        mClassName = msgClass.getSimpleName();
        this.setName("IpcSub: " + mClassName);
        mQueueSize = queueSize;
        mDelegate = delegate;
        mSubscriberName = subscriberName;
    }

    /**
     * Enqueue a new message
     * @param msg - the message to enqueue
     */
    public void queue(T msg) {
        if (!isInterrupted()) {
            if (mQueue.size() == mQueueSize) {
                T discarded = mQueue.poll();
                if (discarded != null) {
                    mMessageStats.numDropped++;
                    mDelegate.onMessageDiscarded(discarded);
                }
            }
            mQueue.add(msg);
            mMessageStats.numReceived++;
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            T msg = null;
            try {
                msg = mQueue.take(); // Blocks until something arrives
                if (mSubscriberName != null) {
                    mMessageStats.printLog();
                }
                mDelegate.onNewMessage(msg);
            } catch (InterruptedException e) {
                break;
            }
        }
        mDelegate.onThreadExit(mQueue);
    }

    /**
     * Returns the number of messages in the queue
     * @return
     */
    int getQueueSize() {
        return mQueue.size();
    }

    /**
     * A class to print the number of message dropped by this thread
     */
    private class MessageStats {
        long numReceived = 0L;
        long numDropped = 0L;
        long lastTimePrinted = -1L;

        void printLog() {
            long now = System.nanoTime();
            // Log every 30 seconds
            if (now - lastTimePrinted > 30E9) {
                Log.d(TAG, String.format("%s dropped %s %d / %d (%.1f %%) ",
                        IpcHandlerThread.this.mSubscriberName, IpcHandlerThread.this.mClassName,
                        numDropped, numReceived,
                        ((float) numDropped / (float) numReceived) * 100.0));
                lastTimePrinted = now;
            }
        }
    }
}
