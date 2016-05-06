package com.darklanders.bitcards.android.engine.device.socket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Chris on 30/04/2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundMessageScan {
    private String content;
}
