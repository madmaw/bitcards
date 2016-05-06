package com.darklanders.bitcards.android.engine.device;

import com.darklanders.bitcards.common.Instruction;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Chris on 30/04/2016.
 */
public abstract class AbstractDeviceClientAdapter implements DeviceClientAdapter {

    private Set<DeviceClientAdapterListener> listeners;

    public AbstractDeviceClientAdapter() {
        this.listeners = new HashSet<DeviceClientAdapterListener>();
    }


    @Override
    public void addListener(DeviceClientAdapterListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(DeviceClientAdapterListener listener) {
        this.listeners.remove(listener);
    }

    public void sendInstruction(Instruction instruction) {
        this.fireInstruction(instruction);
    }

    protected void fireInstruction(Instruction instruction) {
        for( DeviceClientAdapterListener listener : this.listeners ) {
            listener.onInstruction(instruction);
        }
    }

}
