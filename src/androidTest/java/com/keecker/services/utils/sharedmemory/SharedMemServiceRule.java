/**
 * Copyright (C) 2015 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils.sharedmemory;

import com.keecker.services.utils.ServiceTestRule;

public class SharedMemServiceRule extends ServiceTestRule {
    @Override protected void afterService() {
        super.afterService();
        for (SharedMemorySubscriber sub : SharedMemorySubscriber.getActiveSubscribers()) {
            try {
                sub.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
