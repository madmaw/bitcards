package com.darklanders.bitcards.android.engine.device.socket;

import com.darklanders.bitcards.common.Instruction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Chris on 30/04/2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundMessageInstruction {
    private Instruction instruction;
}
