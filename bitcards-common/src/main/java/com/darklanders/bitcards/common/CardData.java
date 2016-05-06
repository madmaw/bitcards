package com.darklanders.bitcards.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by Chris on 27/04/2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardData {

    private String id;
    private String onDrawScript;
    private String onPlayScript;
}
