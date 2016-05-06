package com.darklanders.bitcards.android.engine;

/**
 * Created by Chris on 24/04/2016.
 */
public interface DeviceSource {

    void addDeviceSourceListener(DeviceSourceListener listener);

    void removeDeviceSourceListener(DeviceSourceListener listener);

}
