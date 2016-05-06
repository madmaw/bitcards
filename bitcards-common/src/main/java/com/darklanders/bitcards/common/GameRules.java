package com.darklanders.bitcards.common;

/**
 * Created by Chris on 24/04/2016.
 */
public interface GameRules {

    GameInitData getInitData() throws Exception;

    GamePlayerJoinResult onPlayerJoined(Game game) throws Exception;

}
