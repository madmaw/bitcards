package com.darklanders.bitcards.android.engine.device.socket;

/**
 * Created by Chris on 30/04/2016.
 */
public enum InboundMessageType {
    HEART_BEAT(0),
    SCAN(1),
    TAP(2),
    DEVICE_DATA(3);

    public static final InboundMessageType fromId(int id) {
        for( InboundMessageType type : InboundMessageType.values() ) {
            if( type.getId() == id ) {
                return type;
            }
        }
        return null;
    }


    private int id;

    InboundMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

}
