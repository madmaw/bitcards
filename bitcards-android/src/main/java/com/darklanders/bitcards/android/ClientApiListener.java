package com.darklanders.bitcards.android;

import com.darklanders.bitcards.common.Instruction;

/**
 * Created by Chris on 24/04/2016.
 */
public interface ClientApiListener {

    void onNewInstruction(Instruction instruction);

}
