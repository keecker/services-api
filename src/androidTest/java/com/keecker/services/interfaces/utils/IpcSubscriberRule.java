package com.keecker.services.interfaces.utils;

/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
public class IpcSubscriberRule extends ServiceTestRule {

    @Override protected void afterService() {
        super.afterService();
        for (IpcSubscriber sub : IpcSubscriber.getActiveSubscribers()) {
            try {
                sub.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
