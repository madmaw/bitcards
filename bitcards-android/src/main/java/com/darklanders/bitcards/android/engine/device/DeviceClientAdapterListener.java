package com.darklanders.bitcards.android.engine.device;

import com.darklanders.bitcards.common.Instruction;

/**
 * Created by Chris on 25/04/2016.
 */
public interface DeviceClientAdapterListener {
    void onInstruction(Instruction instruction);
}
