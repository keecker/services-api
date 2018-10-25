// IChargingStationListener.aidl
package com.keecker.services.navigation.interfaces;

import com.keecker.services.navigation.interfaces.PlaneSegmentationResult;

oneway interface IWallSegmentationListener {
      void onWallSegmentation(in PlaneSegmentationResult segmentationResult);
}
