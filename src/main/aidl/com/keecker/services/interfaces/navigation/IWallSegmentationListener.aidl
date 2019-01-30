// IChargingStationListener.aidl
package com.keecker.services.interfaces.navigation;

import com.keecker.services.interfaces.navigation.PlaneSegmentationResult;

oneway interface IWallSegmentationListener {
      void onWallSegmentation(in PlaneSegmentationResult segmentationResult);
}
