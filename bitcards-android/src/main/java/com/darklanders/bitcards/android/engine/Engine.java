package com.darklanders.bitcards.android.engine;

import android.util.Log;
import com.darklanders.bitcards.common.*;

import java.util.*;

/**
 * Created by Chris on 24/04/2016.
 */
public class Engine {

    private Map<Device, Player> devices;
    private GameRules gameRules;
    private Game game;
    private Map<String, Action> activeActions;

    public Engine(DeviceSource deviceSource) {
        this.devices = new HashMap<Device, Player>();
        this.activeActions = new HashMap<String, Action>();

        deviceSource.addDeviceSourceListener(new DeviceSourceListener() {
            @Override
            public void onDeviceJoined(Device device) {
                deviceJoined(device);
            }
        });
    }

    public void deviceJoined(final Device device) {
        Engine.this.devices.put(device, null);

        Instruction instruction;
        if( this.game == null ) {
            instruction = Instruction.NoGame;
        } else {
            instruction = Instruction.Hold;
        }
        device.queueInstruction(instruction, 0, true);

        device.addDeviceListener(new DeviceListener() {
            @Override
            public void onScanCard(String cardId, CardRules cardRules) {
                if( game != null ) {
                    Player player = Engine.this.devices.get(device);
                    if( player != null ) {
                        Action currentAction = Engine.this.activeActions.get(player.getId());

                        if (currentAction != null) {
                            Instruction instruction = device.getCurrentInstruction();
                            if( instruction != Instruction.ScanOk ) {
                                Card card = Engine.this.game.getOrCreateCard(cardId);

                                try {
                                    boolean success;
                                    Runnable runnable = null;
                                    if (card != null) {
                                        switch (instruction) {
                                            case Play:
                                                // is the card able to be played (is it possibly in the player's hand?)
                                                success = player.getHand().containsCard(cardId) || !player.getHand().isExhaustive();
                                                if( success ) {
                                                    CardResultPlay cardResultPlay = cardRules.onPlay(Engine.this.game, player, card);
                                                    runnable = apply(cardResultPlay, player, card);
                                                    success = !cardResultPlay.isFailed();
                                                }
                                                break;
                                            case Draw:
                                                success = !player.getHand().containsCard(cardId) || !player.getHand().isExhaustive();
                                                if( success ) {
                                                    CardResultDraw cardResultDraw = cardRules.onDraw(Engine.this.game, player, card);
                                                    runnable = apply(cardResultDraw, player, card);
                                                    success = !cardResultDraw.isFailed();
                                                }
                                                break;
                                            default:
                                                success = false;
                                                // do nothing
                                        }
                                    } else {
                                        success = false;
                                    }
                                    if( !success ) {
                                        device.queueInstruction(Instruction.InvalidMove, 3000, true);
                                        device.queueInstruction(instruction, 0, false);
                                    } else {
                                        device.queueInstruction(Instruction.ScanOk, 3000, true);
                                        if( runnable != null ) {
                                            runnable.run();
                                        }
                                        // resend all states
                                        for( Map.Entry<Device, Player> device : devices.entrySet() ) {
                                            Player p = device.getValue();
                                            if( p != null ) {
                                                Action move = activeActions.get(p.getId());
                                                if( move != null ) {
                                                    device.getKey().queueInstruction(move.getInstruction(), 0, false);
                                                }
                                            }
                                        }
                                    }
                                } catch( Exception ex ) {
                                    Log.e(Engine.class.getSimpleName(), "unable to perform "+instruction+" on card "+cardId, ex);
                                }
                            }
                        }
                    }
                } else {
                    device.queueInstruction(Instruction.InvalidMove, 2000, true);
                    device.queueInstruction(Instruction.NoGame, 0, false);
                }
            }

            @Override
            public void onScanGame(GameRules gameRules) {
                if( device.getCurrentInstruction() != Instruction.ScanOk ) {
                    try {
                        GameInitData gameInitData = gameRules.getInitData();
                        Game game = new Game(gameInitData);
                        Engine.this.game = game;
                        Engine.this.gameRules = gameRules;
                        // add in any existent players
                        for (Device device : new HashSet<Device>(Engine.this.devices.keySet())) {
                            if( !device.isErroring() ) {
                                device.queueInstruction(Instruction.ScanOk, 3000, true);
                                try {
                                    GamePlayerJoinResult gamePlayerJoinResult = gameRules.onPlayerJoined(game);
                                    // apply the instructions
                                    apply(gamePlayerJoinResult, device);
                                } catch( Exception ex ){
                                    Log.e(Engine.class.getSimpleName(), "error adding player for device "+device, ex);
                                }
                            } else {
                                // remove the device, they'll have to reconnect later
                                Engine.this.devices.remove(device);
                            }
                        }
                    } catch( Exception ex ) {
                        Log.e(Engine.class.getSimpleName(), "error initializing", ex);
                    }

                }
            }

            @Override
            public void onTap() {
                Player player = Engine.this.devices.get(device);
                if( player != null ) {
                    Action currentAction = Engine.this.activeActions.get(player.getId());
                    if (currentAction != null) {
                        CardResult skipCardResult = currentAction.getSkipCardResult();
                        apply(skipCardResult, player);
                    }
                }
            }
        });

        if (Engine.this.gameRules != null && Engine.this.game != null) {
            try {
                GamePlayerJoinResult gamePlayerJoinResult = Engine.this.gameRules.onPlayerJoined(Engine.this.game);
                apply(gamePlayerJoinResult, device);
            } catch( Exception ex ) {
                Log.e(Engine.class.getSimpleName(), "unable to add player for device "+device, ex);
            }
        }

    }

    public void apply(GamePlayerJoinResult gamePlayerJoinResult, Device device) {
        String playerId = Integer.toString(Engine.this.game.getPlayers().size());
        Player player = new Player(playerId);
        player.mergeProperties(gamePlayerJoinResult.getPlayerProperties());
        List<DeckInitData> deckInitData = gamePlayerJoinResult.getDecks();
        if (deckInitData != null) {
            for (DeckInitData deckInitDatum : deckInitData) {
                Deck deck = new Deck(deckInitDatum);
                this.game.setDeck(deck.getId(), deck);
            }
        }
        this.devices.put(device, player);
        this.game.addPlayer(player);
        this.game.mergeProperties(gamePlayerJoinResult.getGameProperties());

        Action playerAction = gamePlayerJoinResult.getPlayerAction();
        if( playerAction == null ) {
            playerAction = new Action(Instruction.Hold);
        }

        this.activeActions.put(player.getId(), playerAction);
        device.queueInstruction(playerAction.getInstruction(), 0, false);

        apply(gamePlayerJoinResult.getActions());
    }

    public Runnable apply(final CardResultDraw cardResultDraw, final Player player, final Card card) {
        return new Runnable() {
            @Override
            public void run() {
                if( !cardResultDraw.isFailed() ) {

                    // remove card from decks
                    Engine.this.game.removeCardIdFromAllDecks(card.getId());
                    String deckId = cardResultDraw.getDeck();
                    if( deckId == null ) {
                        // add card to player's hand
                        player.getHand().addCardId(card.getId());

                    } else {
                        // add directly to the deck
                        Deck deck = Engine.this.game.getDeck(deckId);
                        if( deck == null ) {
                            Log.e(Engine.class.getSimpleName(), "unable to find deck "+deckId);
                        } else {
                            deck.addCardId(card.getId());
                        }
                    }

                    apply(cardResultDraw, player);
                }
            }
        };
    }

    public Runnable apply(final CardResultPlay cardResultPlay, final Player player, final Card card) {
        return new Runnable() {
            @Override
            public void run() {
                if( !cardResultPlay.isFailed() ) {
                    // null indicates it's out of play entirely
                    if( cardResultPlay.getDeck() != null ) {
                        List<String> deckIds = Arrays.asList(cardResultPlay.getDeck());
                        if( deckIds != null ) {
                            if (deckIds.size() == 1) {
                                // find the deck
                                String deckId = deckIds.get(0);
                                Deck deck = Engine.this.game.getDeck(deckId);
                                if (deck != null) {
                                    deck.addCardId(card.getId());
                                } else {
                                    Log.w(Engine.class.getSimpleName(), "no such deck " + deckId);
                                }
                            } else {
                                // mark all target decks as non-exhaustive
                                for( String deckId : deckIds ) {
                                    Deck deck = Engine.this.game.getDeck(deckId);
                                    if( deck != null ) {
                                        deck.setExhaustive(false);
                                    } else {
                                        Log.w(Engine.class.getSimpleName(), "no such deck " + deckId);
                                    }
                                }
                            }
                        }
                    }
                    // remove from player's hand
                    player.getHand().removeCardId(card.getId());

                    apply(cardResultPlay, player);
                }

            }
        };
    }

    public void apply(CardResult cardResult, Player player) {
        if( cardResult != null ) {
            Map<String, Action> actions = cardResult.getActions();
            apply(actions);
            this.game.mergeProperties(cardResult.getGameProperties());
            this.game.mergePlayerProperties(cardResult.getPlayerProperties());
            this.game.mergeCardProperties(cardResult.getCardProperties());
        }
    }

    public Device getDevice(Player player) {
        for( Map.Entry<Device, Player> entry : this.devices.entrySet() ) {
            if( entry.getValue() == player ) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void apply(Map<String, Action> actions) {
        if( actions != null ) {
            for( Map.Entry<String, Action> entry : actions.entrySet() ) {
                String playerId = entry.getKey();
                Action newAction = entry.getValue();
                Action oldAction = this.activeActions.get(playerId);
                // send a message to the device
                Player player = this.game.getPlayer(playerId);
                if( player != null ) {
                    Device device = getDevice(player);
                    // if it's the same action again, send a brief hold signal for feedback
                    if( oldAction != null ) {
                        if( oldAction.getInstruction() == newAction.getInstruction() && oldAction.getInstruction() != Instruction.Hold ) {
                            //device.queueInstruction(Instruction.Hold, 3000, false);
                        }
                    }
                    device.queueInstruction(newAction.getInstruction(), 0, false);
                    this.activeActions.put(playerId, newAction);
                }
            }
        }
    }
}