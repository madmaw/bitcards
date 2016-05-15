package com.darklanders.bitcards.android.engine;

import com.darklanders.bitcards.common.Instruction;

/**
 * Created by Chris on 24/04/2016.
 */
public interface Device {

    String getId();

    void addDeviceListener(DeviceListener listener);

    void removeDeviceListener(DeviceListener listener);

    void queueInstruction(Instruction instruction, long minDisplayMillis, boolean force);

    boolean isErroring();

    Instruction getCurrentInstruction();

}
