package com.darklanders.bitcards.android.engine.device;

import android.util.Log;
import com.darklanders.bitcards.common.Instruction;
import com.darklanders.bitcards.android.ClientApi;
import com.darklanders.bitcards.android.ClientApiListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Chris on 25/04/2016.
 */
public class ClientApiAdapter implements ClientApi {

    private DeviceClientAdapter adapter;
    private Instruction lastInstruction;
    private Set<ClientApiListener> listeners;

    public ClientApiAdapter(DeviceClientAdapter adapter) {
        this.listeners = new HashSet<ClientApiListener>();
        this.lastInstruction = Instruction.Hold;
        this.adapter = adapter;
        this.adapter.addListener(new DeviceClientAdapterListener() {
            @Override
            public void onInstruction(Instruction instruction) {
                setLastInstruction(instruction);
            }
        });
    }

    @Override
    public void addClientApiListener(ClientApiListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeClientApiListener(ClientApiListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public Instruction getLastInstruction() {
        return this.lastInstruction;
    }

    @Override
    public void tap() {
        try {
            this.adapter.fireTapEvent();
        } catch( Exception ex ) {
            Log.e(ClientApiAdapter.class.getSimpleName(), "cannot tap", ex);
        }
    }

    @Override
    public void scan(String content) {
        try {
            this.adapter.fireContentEvent(content);
        } catch ( Exception ex ) {
            Log.e(ClientApiAdapter.class.getSimpleName(), "cannot scan", ex);
        }
    }

    @Override
    public void close() {
        // do nothing
        this.adapter.close();
    }

    public void setLastInstruction(Instruction lastInstruction) {
        this.lastInstruction = lastInstruction;
        for( ClientApiListener listener : this.listeners ) {
            listener.onNewInstruction(lastInstruction);
        }
    }
}
