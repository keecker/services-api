package com.keecker.services.interfaces.navigation;

import com.keecker.services.interfaces.navigation.Odometry;

oneway interface IOdometryListener {
    void onUpdate(in Odometry odometry);
}
