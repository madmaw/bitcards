package com.darklanders.bitcards.common;

/**
 * Created by Chris on 28/04/2016.
 */
public enum ScriptingLanguage {

    JavaScript('J', "JavaScript");

    public static ScriptingLanguage getForShortCode(char shortCode) {
        for( ScriptingLanguage scriptingLanguage : ScriptingLanguage.values() ) {
            if( scriptingLanguage.getShortCode() == shortCode ) {
                return scriptingLanguage;
            }
        }
        return null;
    }

    public static ScriptingLanguage getForLongName(String longName) {
        for( ScriptingLanguage scriptingLanguage : ScriptingLanguage.values() ) {
            if( scriptingLanguage.getLongName().equalsIgnoreCase(longName) ) {
                return scriptingLanguage;
            }
        }
        return null;
    }


    private char shortCode;
    private String longName;

    ScriptingLanguage(char shortCode, String longName) {
        this.shortCode = shortCode;
        this.longName = longName;
    }

    public char getShortCode() {
        return this.shortCode;
    }

    public String getLongName() {
        return this.longName;
    }
}
