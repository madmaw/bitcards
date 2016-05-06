package com.darklanders.bitcards.common.script.rhino;

import com.darklanders.bitcards.common.Game;
import com.darklanders.bitcards.common.GameInitData;
import com.darklanders.bitcards.common.GamePlayerJoinResult;
import com.darklanders.bitcards.common.GameRules;
import org.apache.commons.lang.StringUtils;

/**
 * Created by Chris on 28/04/2016.
 */
public class RhinoGameRules implements GameRules {

    public static final String PARAM_GAME = "game";
    public static final String PARAM_GAME_SHORT = "g";

    public static final String[] EXTERNS = {PARAM_GAME, PARAM_GAME_SHORT};
    private String sourceName;
    private String playerJoinedScript;
    private String initScript;
    private GameInitData initData;

    public RhinoGameRules(String sourceName, String playerJoinedScript, String initScript, GameInitData initData) {
        this.sourceName = sourceName;
        this.playerJoinedScript = playerJoinedScript;
        this.initScript = initScript;
        this.initData = initData;
    }

    @Override
    public GameInitData getInitData() throws Exception {
        if(StringUtils.isEmpty(this.initScript) ) {
            return this.initData;
        } else {

            RhinoHelper helper = new RhinoHelper();
            try {
                return helper.evaluate(this.initScript, GameInitData.class, this.sourceName);
            } finally {
                helper.exit();
            }
        }
    }

    @Override
    public GamePlayerJoinResult onPlayerJoined(Game game) throws Exception {

        RhinoHelper helper = new RhinoHelper();
        try {
            helper.addProperty(game, PARAM_GAME, PARAM_GAME_SHORT);

            return helper.evaluate(this.playerJoinedScript, GamePlayerJoinResult.class, this.sourceName);
        } finally {
            helper.exit();
        }
    }
}
