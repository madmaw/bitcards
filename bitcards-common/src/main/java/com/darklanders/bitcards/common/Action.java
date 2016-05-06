package com.darklanders.bitcards.common;

import lombok.Data;

/**
 * Created by Chris on 24/04/2016.
 */
@Data
public class Action {
    private Instruction instruction;
    private CardResult skipCardResult;

    public Action() {

    }

    public Action(Instruction instruction) {
        this.instruction = instruction;
    }

    public Action(Instruction instruction, CardResult skipCardResult) {
        this.instruction = instruction;
        this.skipCardResult = skipCardResult;
    }

    // shortened getters/settings for JSON
    public Instruction getI() {
        return this.instruction;
    }

    public void setI(Instruction instruction) {
        this.instruction = instruction;
    }

    public CardResult getS() {
        return this.skipCardResult;
    }

    public void setS(CardResult skipCardResult) {
        this.skipCardResult = skipCardResult;
    }
}
