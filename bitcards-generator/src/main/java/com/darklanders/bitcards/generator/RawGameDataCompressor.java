package com.darklanders.bitcards.generator;

import com.darklanders.bitcards.common.ScriptingLanguage;
import com.darklanders.bitcards.common.script.rhino.RhinoCardRules;
import com.darklanders.bitcards.common.script.rhino.RhinoGameRules;
import com.darklanders.bitcards.common.script.rhino.RhinoHelper;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Chris on 28/04/2016.
 */
public class RawGameDataCompressor {

    public static void compress(RawGameData gameData) throws Exception {
        compressScripts(gameData);
    }

    public static void compressScripts(RawGameData gameData) throws Exception {
        ScriptingLanguage language = gameData.getScriptingLanguage();
        for(RawGameData.RawScript script : gameData.getLibraryScripts() ) {
            compressScript(script, language);
        }
        compressScript(gameData.getInitScript(), language);
        compressScript(gameData.getPlayerJoinScript(), language);
        for(RawGameData.RawCardData card : gameData.getCards() ) {
            compressScript(card.getDrawScript(), language);
            compressScript(card.getPlayScript(), language);
        }
    }

    public static void compressScript(RawGameData.RawScript rawScript, ScriptingLanguage language) throws Exception {
        if( rawScript != null ) {
            String code = rawScript.getScript();
            if( code != null ) {
                rawScript.setScript(compressScript(code, language));
            }
        }
    }

    private static String compressScript(String script, ScriptingLanguage language) throws Exception {
        Compiler compiler = new Compiler();

        CompilerOptions options = new CompilerOptions();
        // TODO use advanced?
        CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        options.setRenamingPolicy(VariableRenamingPolicy.ALL, PropertyRenamingPolicy.OFF);


        // externs
        HashSet<String> externalVariables = new HashSet<String>();
        externalVariables.addAll(Arrays.asList(RhinoCardRules.EXTERNS));
        externalVariables.addAll(Arrays.asList(RhinoGameRules.EXTERNS));
        externalVariables.addAll(Arrays.asList(RhinoHelper.EXTERNS));
        String externsString = "function r(){}";
        for( String externalVariable : externalVariables ) {
            externsString += " function "+externalVariable+"(){}";
        }

        SourceFile externs = SourceFile.fromCode("externs.js", externsString);
        SourceFile input = SourceFile.fromCode("input.js", script);

        compiler.compile(externs, input, options);

        return compiler.toSource();
    }

}
