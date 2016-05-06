package com.darklanders.bitcards.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
@AllArgsConstructor
public class GameInitData {
    private List<CardInitData> cards;
    private List<DeckInitData> decks;
    private Map<String, String> properties;

    public GameInitData() {
        this.cards = new ArrayList<CardInitData>();
        this.decks = new ArrayList<DeckInitData>();
        this.properties = new HashMap<String, String>();
    }
}
