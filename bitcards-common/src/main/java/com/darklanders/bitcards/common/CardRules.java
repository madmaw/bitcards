package com.darklanders.bitcards.common;

/**
 * Created by Chris on 24/04/2016.
 */
public interface CardRules {

    CardResultPlay onPlay(Game game, Player player, Card self) throws Exception;

    CardResultDraw onDraw(Game game, Player player, Card self) throws Exception;
}
