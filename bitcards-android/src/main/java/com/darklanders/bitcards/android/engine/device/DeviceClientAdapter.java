package com.darklanders.bitcards.android.engine.device;

import com.darklanders.bitcards.common.Instruction;

/**
 * Created by Chris on 25/04/2016.
 */
public interface DeviceClientAdapter {

    void addListener(DeviceClientAdapterListener listener);

    void removeListener(DeviceClientAdapterListener listener);

    void fireTapEvent() throws Exception;

    void fireContentEvent(String content) throws Exception;

    void close();
}
