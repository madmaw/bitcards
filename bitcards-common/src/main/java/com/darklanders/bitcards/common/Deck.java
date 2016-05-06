package com.darklanders.bitcards.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
public class Deck {
    private String id;
    private List<String> cardIds;
    private Map<String, String> properties;
    private boolean exhaustive;

    public Deck(){
        this(null, null);
    }

    public Deck(String id) {
        this(id, null);
    }

    public Deck(DeckInitData deckInitData) {
        this(deckInitData.getId(), deckInitData);
    }

    public Deck(String id, DeckInitData deckInitData) {
        this.id = id;
        this.cardIds = new ArrayList<String>();
        if( deckInitData == null ) {
            this.properties = new HashMap<String, String>();
        } else {
            this.properties = deckInitData.getProperties();
        }
        this.exhaustive = true;
    }

    public void addCardId(String cardId) {
        this.cardIds.add(cardId);
    }

    public void removeCardId(String cardId) {
        this.cardIds.remove(cardId);
    }

    public boolean containsCard(String cardId) {
        return this.cardIds.contains(cardId);
    }

    // for javascript
    public String[] getCs() {
        return this.cardIds.toArray(new String[this.cardIds.size()]);
    }
}
