package com.darklanders.bitcards.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Chris on 27/04/2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameData {
    private ScriptingLanguage scriptLanguage;
    private String id;
    private String onPlayerJoinScript;
    private String initScript;
    private GameInitData initData;
}
