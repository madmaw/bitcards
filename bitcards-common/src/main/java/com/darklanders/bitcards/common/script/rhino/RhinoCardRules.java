package com.darklanders.bitcards.common.script.rhino;

import com.darklanders.bitcards.common.*;

/**
 * Created by Chris on 28/04/2016.
 */
public class RhinoCardRules implements CardRules {

    public static final String PARAM_GAME = "game";
    public static final String PARAM_GAME_SHORT = "g";

    public static final String PARAM_PLAYER = "player";
    public static final String PARAM_PLAYER_SHORT = "p";

    public static final String PARAM_CARD = "card";
    public static final String PARAM_CARD_SHORT = "c";

    public static final String[] EXTERNS = {PARAM_CARD, PARAM_CARD_SHORT, PARAM_GAME, PARAM_GAME_SHORT, PARAM_PLAYER, PARAM_PLAYER_SHORT};

    private String sourceName;

    private String playScript;
    private String drawScript;

    public RhinoCardRules(String sourceName, String playScript, String drawScript) {
        this.sourceName = sourceName;
        this.playScript = playScript;
        this.drawScript = drawScript;
    }

    @Override
    public CardResultPlay onPlay(Game game, Player player, Card self) throws Exception {

        RhinoHelper helper = new RhinoHelper();
        try {
            helper.addProperty(game, PARAM_GAME, PARAM_GAME_SHORT);

            helper.addProperty(self, PARAM_CARD, PARAM_CARD_SHORT);

            helper.addProperty(player, PARAM_PLAYER, PARAM_PLAYER_SHORT);

            return helper.evaluate(this.playScript, CardResultPlay.class, sourceName);
        } finally {
            helper.exit();
        }
    }

    @Override
    public CardResultDraw onDraw(Game game, Player player, Card self) throws Exception {
        RhinoHelper helper = new RhinoHelper();
        try {

            helper.addProperty(game, PARAM_GAME, PARAM_GAME_SHORT);

            helper.addProperty(self, PARAM_CARD, PARAM_CARD_SHORT);

            helper.addProperty(player, PARAM_PLAYER, PARAM_PLAYER_SHORT);

            return helper.evaluate(this.drawScript, CardResultDraw.class, sourceName);
        } finally {
            helper.exit();
        }
    }
}
