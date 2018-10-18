package com.keecker.services.navigation.interfaces;

import com.keecker.services.navigation.interfaces.Odometry;

oneway interface IOdometryListener {
    void onUpdate(in Odometry odometry);
}