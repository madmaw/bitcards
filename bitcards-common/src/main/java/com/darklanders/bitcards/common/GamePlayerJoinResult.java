package com.darklanders.bitcards.common;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
public class GamePlayerJoinResult {
    private Map<String, Action> actions;
    private List<DeckInitData> decks;
    private Action playerAction;
    private Map<String, String> playerProperties;
    private Map<String, String> gameProperties;

    // shortened getters/setters for JavaScript
    public Action getPa() {
        return this.playerAction;
    }

    public void setPa(Action playerAction) {
        this.playerAction = playerAction;
    }

    public Map<String, String> getPp() {
        return this.playerProperties;
    }

    public void setPp(Map<String, String> playerProperties) {
        this.playerProperties = playerProperties;
    }

    public Map<String, String> getGp() {
        return this.gameProperties;
    }

    public void setGp(Map<String, String> gameProperties) {
        this.gameProperties = gameProperties;
    }
}
