package com.darklanders.bitcards.android;

import com.darklanders.bitcards.common.Instruction;

/**
 * Created by Chris on 24/04/2016.
 */
public interface ClientApi {

    void addClientApiListener(ClientApiListener listener);

    void removeClientApiListener(ClientApiListener listener);

    Instruction getLastInstruction();

    void tap();

    void scan(String content);

    void close();
}
