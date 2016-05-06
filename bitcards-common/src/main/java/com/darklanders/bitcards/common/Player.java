package com.darklanders.bitcards.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
public class Player {
    private String id;
    private Deck hand;
    private Map<String, String> properties;

    public Player(String id) {
        this.id = id;
        this.hand = new Deck(id);
        this.properties = new HashMap<String, String>();
    }

    public void mergeProperties(Map<String, String> properties) {
        if( properties != null ) {
            this.properties.putAll(properties);
        }
    }

    public void removeCardId(String cardId) {
        this.hand.removeCardId(cardId);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Player) {
            Player p = (Player)o;
            return this.id.equals(p.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    // javascript stuff
    public Map<String, String> getPs() {
        return this.properties;
    }


}
