package com.darklanders.bitcards.common;

import lombok.Data;

import java.util.*;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
public class Game {

    private List<Player> players;
    private Map<String, Card> cards;
    private Deck drawDeck;
    private Map<String, Deck> decks;
    private Map<String, String> properties;

    public Game(GameInitData init) {
        this.players = new ArrayList<Player>();
        this.drawDeck = new Deck();
        this.decks = new HashMap<String, Deck>();
        this.cards = new HashMap<String, Card>();
        if( init != null ) {
            this.properties = init.getProperties();
        } else {
            this.properties = new HashMap<String, String>();
        }
        for( CardInitData cardInitData : init.getCards() ) {
            this.cards.put(cardInitData.getId(), new Card(cardInitData));
            // assume all cards go into the draw deck to start
            this.drawDeck.addCardId(cardInitData.getId());
        }
        for( DeckInitData deckInitData : init.getDecks() ) {
            this.decks.put(deckInitData.getId(), new Deck(deckInitData));
        }
    }

    public Card getOrCreateCard(String cardId) {
        Card card = this.cards.get(cardId);
        if( card == null ) {
            card = new Card(cardId);
            this.cards.put(cardId, card);
        }
        return card;
    }

    public void removeCardIdFromAllDecks(String cardId) {
        this.drawDeck.removeCardId(cardId);
        for( Deck deck : this.decks.values() ) {
            deck.removeCardId(cardId);
        }
        for( Player player : this.players ) {
            player.removeCardId(cardId);
        }
    }

    public Deck getDeck(String deckId) {
        return this.decks.get(deckId);
    }

    public void setDeck(String id, Deck deck) {
        this.decks.put(id, deck);
    }

    public Player getPlayer(String id) {
        for( Player player : this.players ) {
            if( player.getId().equals(id) ) {
                return player;
            }
        }
        return null;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void mergeProperties(Map<String, String> properties) {
        if( properties != null ) {
            this.properties.putAll(properties);
        }
    }

    public void mergePlayerProperties(Map<String, Map<String, String>> playerProperties) {
        if( playerProperties != null ) {
            for( Map.Entry<String, Map<String, String>> entry : playerProperties.entrySet() ) {
                Player player = getPlayer(entry.getKey());
                if( player != null ) {
                    player.mergeProperties(entry.getValue());
                }
            }
        }

    }

    public void mergeCardProperties(Map<String, Map<String, String>> cardProperties) {
        if( cardProperties != null ) {
            for( Map.Entry<String, Map<String, String>> entry : cardProperties.entrySet() ) {
                Card card = getOrCreateCard(entry.getKey());
                if( card != null ) {
                    card.mergeProperties(entry.getValue());
                }
            }
        }
    }

    // shortened for JavaScript
    public Map<String, String> getPs() {
        return this.properties;
    }

    public void setPs(Map<String, String> properties) {
        this.properties = properties;
    }

    public Player[] getPys() {
        return this.players.toArray(new Player[this.players.size()]);
    }

    public Map<String, Card> getCs() {
        return this.cards;
    }

}
