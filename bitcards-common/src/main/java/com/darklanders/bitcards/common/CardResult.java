package com.darklanders.bitcards.common;

import lombok.Data;

import java.util.Map;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
public class CardResult {
    boolean failed;
    Map<String, Action> actions;
    Map<String, Map<String, String>> cardProperties;
    Map<String, Map<String, String>> playerProperties;
    Map<String, String> gameProperties;

    // shortened for javascript

    public boolean isF() {
        return this.failed;
    }

    public void setF(boolean failed) {
        this.failed = failed;
    }

    public Map<String, Action> getAs() {
        return this.actions;
    }

    public void setAs(Map<String, Action> actions) {
        this.actions = actions;
    }

    public Map<String, Map<String, String>> getCps() {
        return this.cardProperties;
    }

    public void setCps(Map<String, Map<String, String>> cardProperties) {
        this.cardProperties = cardProperties;
    }

    public Map<String, Map<String, String>> getPps() {
        return this.playerProperties;
    }

    public void setPps(Map<String, Map<String, String>> playerProperties) {
        this.playerProperties = playerProperties;
    }

    public Map<String, String> getGps() {
        return this.gameProperties;
    }

    public void setGps(Map<String, String> gameProperties) {
        this.gameProperties = gameProperties;
    }
}
