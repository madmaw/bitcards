package com.darklanders.bitcards.android.engine.device;

import com.darklanders.bitcards.common.Instruction;
import lombok.Data;

/**
 * Created by Chris on 25/04/2016.
 */
public class ScriptedDeviceClientAdapter extends AbstractDeviceClientAdapter {

    private ScriptedDevice device;

    public ScriptedDeviceClientAdapter(ScriptedDevice device) {
        this.device = device;
    }

    @Override
    public void fireTapEvent() {
        this.device.fireTapEvent();
    }

    @Override
    public void fireContentEvent(String content) {
        this.device.fireScanEvent(content);
    }

    @Override
    public void close() {
        // do nothing
    }

    public boolean isDead() {
        return this.device.isErroring();
    }

    public void setDead(boolean dead) {
        this.device.setErroring(dead);
    }
}
