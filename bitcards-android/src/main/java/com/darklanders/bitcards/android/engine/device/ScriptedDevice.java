package com.darklanders.bitcards.android.engine.device;

import android.util.Log;
import com.darklanders.bitcards.common.*;
import com.darklanders.bitcards.android.engine.Device;
import com.darklanders.bitcards.android.engine.DeviceListener;
import com.darklanders.bitcards.common.script.rhino.RhinoCardRules;
import com.darklanders.bitcards.common.script.rhino.RhinoGameRules;
import com.darklanders.bitcards.common.script.rhino.RhinoHelper;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by Chris on 25/04/2016.
 */
public class ScriptedDevice implements Device {

    private class QueueChecker extends Thread {
        boolean running = true;
        @Override
        public void run() {
            while( this.running ) {
                synchronized (ScriptedDevice.this) {
                    QueuedInstruction queuedInstruction = instructions.peek();
                    if( queuedInstruction != null && (queuedInstruction.getMinDisplayMillis() > 0 || instructions.size() <= 1) ) {
                        long displayTime;
                        if( queuedInstruction.getInstruction() != standingInstruction ) {
                            displayTime = queuedInstruction.getMinDisplayMillis();
                        } else {
                            displayTime = queuedInstruction.getMinDisplayMillis() - System.currentTimeMillis() + standingInstructionStartTime;
                        }
                        if( displayTime > 0 || instructions.size() == 1 ) {
                            setStandingInstruction(queuedInstruction.getInstruction());
                        }
                        if( displayTime > 0 ) {
                            try {
                                ScriptedDevice.this.wait(displayTime);
                            } catch( InterruptedException ex ) {

                            }
                        }
                    }
                    // remove the head now
                    instructions.poll();
                    if( instructions.peek() == null ) {
                        this.running = false;
                    }
                }
            }
        }

        public void cancel() {
            this.running = false;
        }
    }


    private Set<DeviceListener> listeners;
    private Instruction standingInstruction;
    private long standingInstructionStartTime;
    private Queue<QueuedInstruction> instructions;
    private ScriptedDeviceClientAdapter clientAdapter;
    private QueueChecker queueChecker = new QueueChecker();
    private String id;
    private boolean erroring;

    public ScriptedDevice(String id) {
        this.listeners = new HashSet<DeviceListener>();
        this.instructions = new LinkedList<QueuedInstruction>();
        this.id = id;
    }

    public void setClientAdapter(ScriptedDeviceClientAdapter clientAdapter) {
        this.clientAdapter = clientAdapter;
        this.standingInstruction = null;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void addDeviceListener(DeviceListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeDeviceListener(DeviceListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void queueInstruction(Instruction instruction, long minDisplayMillis, boolean force) {
        synchronized(this) {
            if( force ) {
                this.instructions.clear();
            }
            boolean immediate = this.instructions.size() == 0;
            QueuedInstruction queuedInstruction = new QueuedInstruction(instruction, minDisplayMillis);
            this.instructions.add(queuedInstruction);
            if( immediate ) {
                if( this.standingInstruction != queuedInstruction.getInstruction() ) {
                    this.setStandingInstruction(queuedInstruction.getInstruction());
                }
                if( this.queueChecker != null ) {
                    this.queueChecker.cancel();
                }
                this.queueChecker = new QueueChecker();
                this.queueChecker.start();
            }
        }
    }

    @Override
    public boolean isErroring() {
        return this.erroring;
    }

    public void setErroring(boolean erroring) {
        this.erroring = erroring;
    }

    public void setStandingInstruction(Instruction standingInstruction) {
        if( this.standingInstruction != standingInstruction ) {
            this.standingInstruction = standingInstruction;
            this.standingInstructionStartTime = System.currentTimeMillis();
            this.clientAdapter.sendInstruction(standingInstruction);
        }
    }

    public void fireTapEvent(){
        for( DeviceListener listener : this.listeners ) {
            listener.onTap();
        }
    }

    public void fireScanEvent(String content) {
        // convert to appropriate types
        synchronized (this) {
            if( this.instructions.size() == 0 ) {
                try {
                    // only allow scans if we haven't got queued instructions
                    IOHelper.BitReader ioHelper = new IOHelper.BitReader(new StringReader(content), content);
                    Object read = ioHelper.read();


                    if( read instanceof GameData ) {
                        GameData gameData = (GameData)read;
                        RhinoGameRules gameRules = new RhinoGameRules(gameData.getId(), gameData.getOnPlayerJoinScript(), gameData.getInitScript(), gameData.getInitData());
                        for( DeviceListener listener : this.listeners ) {
                            listener.onScanGame(gameRules);
                        }
                    } else if( read instanceof CardData ) {
                        CardData cardData = (CardData)read;
                        RhinoCardRules cardRules = new RhinoCardRules(cardData.getId(), cardData.getOnPlayScript(), cardData.getOnDrawScript());
                        for( DeviceListener listener : this.listeners ) {
                            listener.onScanCard(cardData.getId(), cardRules);
                        }

                    } else {
                        Log.w(ScriptedDevice.class.getSimpleName(), "unrecognised object "+read+" in "+content);
                    }
                } catch ( Exception ex ) {
                    Log.e(ScriptedDevice.class.getSimpleName(), "cannot scan "+content, ex);
                }
            }

        }
    }

    @Override
    public Instruction getCurrentInstruction() {
        return this.standingInstruction;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if( obj instanceof Device ) {
            return equals((Device)obj);
        } else {
            return false;
        }
    }

    public boolean equals(Device device) {
        if( device != null ) {
            return this.id.equals(device.getId());
        } else {
            return false;
        }
    }
}
