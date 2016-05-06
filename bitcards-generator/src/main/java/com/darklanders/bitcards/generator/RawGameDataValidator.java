package com.darklanders.bitcards.generator;

import com.darklanders.bitcards.common.Game;
import com.darklanders.bitcards.common.GameInitData;
import com.darklanders.bitcards.common.GamePlayerJoinResult;
import com.darklanders.bitcards.common.script.rhino.RhinoCardRules;
import com.darklanders.bitcards.common.script.rhino.RhinoGameRules;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Created by Chris on 28/04/2016.
 */
public class RawGameDataValidator {
    private static void assertTrue(boolean thing, String message) throws Exception {
        if( !thing ) {
            throw new Exception(message);
        }
    }

    public static void assertFieldExists(Object value, String elementName, String parentElementName) throws Exception {
        assertTrue(value != null, elementName+" must be specified in "+parentElementName);
    }

    public static final void validate(RawGameData gameData) throws Exception {
        assertFieldExists(gameData.getScriptingLanguage(), RawGameData.ELEM_SCRIPT_LANGUAGE, "Game");
        assertFieldExists(gameData.getPlayerJoinScript(), RawGameData.ELEM_PLAYER_JOIN_SCRIPT, "Game");
        assertGameScriptsRun(gameData, "Game");
    }

    public static final void assertGameScriptsRun(
            RawGameData gameData,
            String elementName
    ) throws Exception {
        RawGameData.RawScriptOrReference initScriptOrReference = gameData.getInitScript();
        String initScript = gameData.getScript(initScriptOrReference);

        RawGameData.RawScriptOrReference playerJoinScriptOrReference = gameData.getPlayerJoinScript();
        String playerJoinScript = gameData.getScript(playerJoinScriptOrReference);
        assertTrue(playerJoinScript != null, "can't find "+RawGameData.ELEM_PLAYER_JOIN_SCRIPT+" in "+elementName);

        GameInitData originalGameInitData = gameData.toInitData();

        RhinoGameRules gameRules = new RhinoGameRules(elementName, playerJoinScript, initScript, originalGameInitData);

        GameInitData gameInitData = gameRules.getInitData();

        // TODO validate that all the cards appear in the game init data (and that there are no extras)

        Game game = new Game(gameInitData);

        GamePlayerJoinResult joinResult = gameRules.onPlayerJoined(game);

        // well, there were no errors?
        System.out.println(joinResult);
    }

}
