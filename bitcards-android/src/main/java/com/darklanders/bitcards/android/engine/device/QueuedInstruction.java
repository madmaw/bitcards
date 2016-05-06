package com.darklanders.bitcards.android.engine.device;

import com.darklanders.bitcards.common.Instruction;
import lombok.Data;

/**
 * Created by Chris on 25/04/2016.
 */
@Data
public class QueuedInstruction {
    private Instruction instruction;
    private long minDisplayMillis;

    public QueuedInstruction(Instruction instruction, long minDisplayMillis) {
        this.instruction = instruction;
        this.minDisplayMillis = minDisplayMillis;
    }
}
