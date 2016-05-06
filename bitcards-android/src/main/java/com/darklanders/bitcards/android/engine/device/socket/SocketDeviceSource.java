package com.darklanders.bitcards.android.engine.device.socket;

import android.util.Log;
import com.darklanders.bitcards.android.engine.Device;
import com.darklanders.bitcards.android.engine.DeviceSource;
import com.darklanders.bitcards.android.engine.DeviceSourceListener;
import com.darklanders.bitcards.android.engine.device.ScriptedDevice;
import com.darklanders.bitcards.android.engine.device.ScriptedDeviceClientAdapter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Chris on 30/04/2016.
 */
public class SocketDeviceSource implements DeviceSource {

    private class ServerSocketRunnable implements  Runnable {

        private boolean running;
        private Map<String, SocketDeviceClientAdapterSkeleton> skeletons;

        public ServerSocketRunnable() {
            this.running = true;
            this.skeletons = new HashMap<String, SocketDeviceClientAdapterSkeleton>();
        }

        @Override
        public void run() {
            try {
                while( this.running ) {
                    Socket socket = serverSocket.accept();
                    if( this.running ) {
                        SocketIOHelper ioHelper = new SocketIOHelper(socket);
                        InboundMessageType inboundMessageType = ioHelper.readInboundMessageType();
                        if( inboundMessageType == InboundMessageType.DEVICE_DATA ) {
                            InboundMessageDeviceData deviceData = ioHelper.readInboundMessageDeviceData();
                            SocketDeviceClientAdapterSkeleton skeleton = skeletons.get(deviceData.getId());
                            if( skeleton == null ) {
                                ScriptedDevice device = new ScriptedDevice(deviceData.getId());
                                ScriptedDeviceClientAdapter deviceClientAdapter = new ScriptedDeviceClientAdapter(device);
                                device.setClientAdapter(deviceClientAdapter);
                                skeleton = new SocketDeviceClientAdapterSkeleton(deviceClientAdapter);
                                skeleton.start(socket);

                                fireDeviceJoined(device);
                                this.skeletons.put(deviceData.getId(), skeleton);
                            } else {
                                // restart with a new socket
                                skeleton.stop();
                                skeleton.start(socket);
                            }

                        } else {
                            Log.w(SocketDeviceSource.class.getSimpleName(), "unexpected message type "+inboundMessageType);
                        }
                    }
                }
            } catch ( Exception ex ) {
                Log.e(SocketDeviceSource.class.getSimpleName(), "error obtaining socket", ex);
            }
        }

        public void stop() {
            this.running = false;
            for( SocketDeviceClientAdapterSkeleton skeleton : this.skeletons.values() ) {
                skeleton.stop();
            }
        }
    }

    public static final int PORT = 7234;

    private HashSet<DeviceSourceListener> listeners;

    private ServerSocket serverSocket;
    private ServerSocketRunnable serverSocketRunnable;

    public SocketDeviceSource() {
        this.listeners = new HashSet<DeviceSourceListener>();
    }

    @Override
    public void addDeviceSourceListener(DeviceSourceListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeDeviceSourceListener(DeviceSourceListener listener) {
        this.listeners.remove(listener);
    }

    protected void fireDeviceJoined(Device device) {
        for( DeviceSourceListener listener : this.listeners ) {
            listener.onDeviceJoined(device);
        }
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.serverSocketRunnable = new ServerSocketRunnable();
        Thread thread = new Thread(this.serverSocketRunnable);
        thread.start();
    }

    public void stop() throws IOException {
        this.serverSocket.close();
        this.serverSocketRunnable.stop();
    }
}
