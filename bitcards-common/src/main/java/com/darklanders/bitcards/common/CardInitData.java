package com.darklanders.bitcards.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */

@Data
@AllArgsConstructor
public class CardInitData {

    private String id;
    private Map<String, String> properties;

    public CardInitData(String id) {
        this.id = id;
        this.properties = new HashMap<String, String>();
    }

    public void mergeProperties(Map<String, String> properties) {
        if( this.properties != null ) {
            this.properties.putAll(properties);
        }
    }
}
