package com.darklanders.bitcards.android.engine.device.socket;

import android.util.Log;
import com.darklanders.bitcards.android.engine.device.DeviceClientAdapter;
import com.darklanders.bitcards.android.engine.device.DeviceClientAdapterListener;
import com.darklanders.bitcards.android.engine.device.ScriptedDeviceClientAdapter;
import com.darklanders.bitcards.common.Instruction;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Chris on 30/04/2016.
 */
public class SocketDeviceClientAdapterSkeleton {

    private class SocketReader implements Runnable {
        private boolean running;

        public SocketReader() {
            this.running = true;
        }

        @Override
        public void run() {
            try {
                while( this.running ) {
                    InboundMessageType messageType = ioHelper.readInboundMessageType();
                    if( messageType != null ) {
                        switch (messageType) {
                            case DEVICE_DATA:
                            case HEART_BEAT:
                                break;
                            case SCAN:
                                deviceClientAdapter.fireContentEvent(ioHelper.readInboundMessageScan().getContent());
                                break;
                            case TAP:
                                deviceClientAdapter.fireTapEvent();
                                break;
                        }
                    } else {
                        //Log.w(SocketDeviceClientAdapterSkeleton.class.getSimpleName(), "unrecognised message");
                        // send a heartbeat
                        ioHelper.writeOutboundMessageType(OutboundMessageType.HEART_BEAT);
                    }
                    synchronized (this) {
                        this.wait(100);
                    }
                }
            } catch( Exception ex ) {
                Log.e(SocketDeviceClientAdapterSkeleton.class.getSimpleName(), "error reading socket", ex);
                deviceClientAdapter.setDead(true);
            }

        }

        public void stop() {
            this.running = false;
            synchronized(this) {
                this.notify();
            }
        }
    }

    private ScriptedDeviceClientAdapter deviceClientAdapter;
    private DeviceClientAdapterListener deviceClientAdapterListener;
    private SocketIOHelper ioHelper;
    private SocketReader socketReader;
    private Instruction lastInstruction;

    public SocketDeviceClientAdapterSkeleton(ScriptedDeviceClientAdapter deviceClientAdapter) {
        this.deviceClientAdapter = deviceClientAdapter;
        this.deviceClientAdapterListener = new DeviceClientAdapterListener() {
            @Override
            public synchronized void onInstruction(Instruction instruction) {
                lastInstruction = instruction;
                writeInstruction(instruction);
            }
        };
    }

    public void start(Socket socket) throws IOException {
        this.deviceClientAdapter.setDead(false);
        this.ioHelper = new SocketIOHelper(socket);
        this.deviceClientAdapter.addListener(this.deviceClientAdapterListener);
        this.socketReader = new SocketReader();
        Thread thread = new Thread(this.socketReader);
        thread.start();
        if( this.lastInstruction != null ) {
            writeInstruction(this.lastInstruction);
        }
    }

    public void stop() {
        this.ioHelper.stop();
        this.deviceClientAdapter.removeListener(this.deviceClientAdapterListener);
        this.socketReader.stop();
    }

    private void writeInstruction(Instruction instruction) {
        try {
            ioHelper.writeOutboundMessageType(OutboundMessageType.INSTRUCTION);
            ioHelper.writeOutboundMessageInstruction(new OutboundMessageInstruction(instruction));
        } catch( Exception ex ) {
            Log.e(SocketDeviceClientAdapterSkeleton.class.getSimpleName(), "unable to send instruction", ex);
        }
    }

}
