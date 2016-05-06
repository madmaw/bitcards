package com.darklanders.bitcards.common;

import com.darklanders.bitcards.common.CardInitData;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */

@Data
public class Card {

    private String id;
    private Map<String, String> properties;

    public Card(CardInitData data) {
        this.id = data.getId();
        this.properties = new HashMap<String, String>(data.getProperties());
    }

    public Card(String id) {
        this.id = id;
        this.properties = new HashMap<String, String>();
    }

    public void mergeProperties(Map<String, String> properties) {
        if( this.properties != null ) {
            this.properties.putAll(properties);
        }
    }
}
