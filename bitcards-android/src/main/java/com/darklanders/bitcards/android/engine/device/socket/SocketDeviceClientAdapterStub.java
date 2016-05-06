package com.darklanders.bitcards.android.engine.device.socket;

import android.app.Activity;
import android.util.Log;
import com.darklanders.bitcards.android.engine.device.AbstractDeviceClientAdapter;
import com.darklanders.bitcards.android.util.APManager;
import com.darklanders.bitcards.common.Instruction;

import java.io.*;
import java.net.Socket;

/**
 * Created by Chris on 30/04/2016.
 */
public class SocketDeviceClientAdapterStub extends AbstractDeviceClientAdapter {

    private class SocketReader implements Runnable {
        private boolean running;
        private String host;

        public SocketReader() {
            this.running = true;
        }

        @Override
        public void run() {
            Socket localSocket = null;


            while( this.running ) {

                try {
                    // check we're connected to the right server
                    String host;
                    if(APManager.isApOn(activity) || !APManager.isWifiConnected(activity)) {
                        host = "localhost";
                    } else {
                        host = APManager.getApIP(activity);
                    }

                    if( !host.equals(this.host) ) {
                        this.host = host;
                        if( localSocket != null ) {
                            try {
                                localSocket.close();
                            } catch( Exception ex ) {
                                // do nothing
                            }
                            localSocket = null;
                        }
                    }

                    if( localSocket == null || localSocket.isClosed() || !localSocket.isConnected() ) {
                        fireInstruction(Instruction.Disconnected);
                        localSocket = new Socket(host, SocketDeviceSource.PORT);
                        ioHelper = new SocketIOHelper(localSocket);

                        ioHelper.writeInboundMessageType(InboundMessageType.DEVICE_DATA);
                        ioHelper.writeInboundMessageDeviceData(new InboundMessageDeviceData(deviceId));

                    }

                    OutboundMessageType messageType = ioHelper.readOutboundMessageType();
                    if (messageType != null) {
                        switch (messageType) {
                            case INSTRUCTION:
                                fireInstruction(ioHelper.readOutboundMessageInstruction().getInstruction());
                                break;
                            case HEART_BEAT:
                                break;
                            default:
                                Log.w(SocketDeviceClientAdapterStub.class.getSimpleName(), "unrecognised instruction " + messageType);

                        }
                    } else {
                        // fire a keep alive to see
                        ioHelper.writeInboundMessageType(InboundMessageType.HEART_BEAT);
                    }
                } catch( Exception ex ) {
                    // TODO handle exception
                    Log.e(SocketDeviceClientAdapterStub.class.getSimpleName(), "error reading socket", ex);
                    try {
                        localSocket.close();
                    } catch ( Exception ex2 ) {

                    }
                    localSocket = null;
                }
                synchronized (this) {
                    try {
                        this.wait(100);
                    } catch( InterruptedException ex ) {

                    }
                }
            }
            try {
                localSocket.close();
            } catch( Exception ex ) {

            }
        }

        public void stop() {
            this.running = false;
            synchronized (this) {
                this.notify();
            }
        }
    }

    private Activity activity;
    private SocketIOHelper ioHelper;
    private SocketReader socketReader;
    private String deviceId;

    public SocketDeviceClientAdapterStub(Activity activity, String deviceId) {
        this.activity = activity;
        this.deviceId = deviceId;
    }

    @Override
    public synchronized void fireTapEvent() throws Exception {
        if( this.ioHelper != null ) {

            // send it down the socket
            this.ioHelper.writeInboundMessageType(InboundMessageType.TAP);
        } else {
            Log.w(SocketDeviceSource.class.getSimpleName(), "unable to send tap event, socket not ready");
        }
    }

    @Override
    public synchronized void fireContentEvent(String content) throws Exception {
        if( this.ioHelper != null ) {
            this.ioHelper.writeInboundMessageType(InboundMessageType.SCAN);
            this.ioHelper.writeInboundMessageScan(new InboundMessageScan(content));
        } else {
            Log.w(SocketDeviceSource.class.getSimpleName(), "unable to send content event, socket not ready");
        }
    }

    public void start() throws IOException {
        this.socketReader = new SocketReader();
        Thread thread = new Thread(this.socketReader);
        thread.start();
    }

    @Override
    public void close() {
        this.stop();
    }

    public void stop() {
        if( this.ioHelper != null ) {
            this.ioHelper.stop();
        }
        if( this.socketReader != null ) {
            this.socketReader.stop();
            this.socketReader = null;
        }
    }
}
