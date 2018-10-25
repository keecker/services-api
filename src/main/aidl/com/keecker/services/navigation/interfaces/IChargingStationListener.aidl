// IChargingStationListener.aidl
package com.keecker.services.navigation.interfaces;

import com.keecker.services.navigation.interfaces.DetectionPose;

oneway interface IChargingStationListener {
      void onChargingStationPerceived(in DetectionPose detectionPose);
}
