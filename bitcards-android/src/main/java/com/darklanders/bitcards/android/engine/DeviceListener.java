package com.darklanders.bitcards.android.engine;

import com.darklanders.bitcards.common.CardRules;
import com.darklanders.bitcards.common.GameRules;

/**
 * Created by Chris on 24/04/2016.
 */
public interface DeviceListener {

    void onScanCard(String cardId, CardRules cardRules);

    void onScanGame(GameRules gameRules);

    void onTap();

}
