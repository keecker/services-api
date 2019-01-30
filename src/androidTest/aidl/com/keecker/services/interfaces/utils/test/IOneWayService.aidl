package com.keecker.services.interfaces.utils.test;

import com.keecker.services.interfaces.utils.test.OneWayListener;

interface IOneWayService {
    void subscribeToOneWayListener(in OneWayListener listener);
    void floodOneWayListener(int count, long delay);
}
