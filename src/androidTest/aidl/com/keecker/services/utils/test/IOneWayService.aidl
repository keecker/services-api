package com.keecker.services.utils.test;

import com.keecker.services.utils.test.OneWayListener;

interface IOneWayService {
    void subscribeToOneWayListener(in OneWayListener listener);
    void floodOneWayListener(int count, long delay);
}