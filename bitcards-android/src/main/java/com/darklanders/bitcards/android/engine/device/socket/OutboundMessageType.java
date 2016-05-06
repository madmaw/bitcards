package com.darklanders.bitcards.android.engine.device.socket;

/**
 * Created by Chris on 30/04/2016.
 */
public enum OutboundMessageType {

    HEART_BEAT(0),
    INSTRUCTION(1);

    public static OutboundMessageType fromId(int id) {
        for(OutboundMessageType type : OutboundMessageType.values()) {
            if( type.getId() == id) {
                return type;
            }
        }
        return null;
    }


    private int id;

    OutboundMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
