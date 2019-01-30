// IChargingStationListener.aidl
package com.keecker.services.interfaces.navigation;

import com.keecker.services.interfaces.navigation.DetectionPose;

oneway interface IChargingStationListener {
      void onChargingStationPerceived(in DetectionPose detectionPose);
}
