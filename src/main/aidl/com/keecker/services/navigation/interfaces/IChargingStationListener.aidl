// IChargingStationListener.aidl
package com.keecker.services.navigation.interfaces;

import com.keecker.services.navigation.interfaces.DetectionResult;

oneway interface IChargingStationListener {
      void onChargingStationPerceived(in DetectionResult detectionResult);
}
