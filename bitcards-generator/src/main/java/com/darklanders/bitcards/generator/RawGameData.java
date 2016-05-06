package com.darklanders.bitcards.generator;

import com.darklanders.bitcards.common.*;
import lombok.Data;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 28/04/2016.
 */
@Data
@XmlRootElement(name = "Game")
@XmlAccessorType(XmlAccessType.FIELD)
public class RawGameData {

    public static Map<String, String> toProperties(List<RawProperty> rawProperties) {
        HashMap<String, String> properties;
        if( rawProperties != null ) {
            properties = new HashMap<String, String>(rawProperties.size());
            for( RawProperty rawProperty : rawProperties ) {
                properties.put(rawProperty.getKey(), rawProperty.getValue());
            }
        } else {
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RawProperty {
        @XmlAttribute
        String key;
        @XmlValue
        String value;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RawDeckData {
        @XmlAttribute
        private String id;
        @XmlElement(name = "Property")
        private List<RawProperty> properties;

        public DeckInitData toInitData() {
            return new DeckInitData(this.id, toProperties(this.properties));
        }
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "Card")
    public static class RawCardData {
        @XmlAttribute
        private String id;
        @XmlAttribute
        private String title;
        @XmlAttribute(name = "qr-code-path")
        private String qrCodePath;
        @XmlAttribute(name = "qr-code-width")
        private Integer qrCodeWidth;
        @XmlAttribute(name = "qr-code-height")
        private Integer qrCodeHeight;
        @XmlElement(name = "Description")
        private String description;
        @XmlElement(name = "DrawScript")
        private RawScriptOrReference drawScript;
        @XmlElement(name = "PlayScript")
        private RawScriptOrReference playScript;
        @XmlElement(name = "Property")
        private List<RawProperty> properties;


        public CardData toData(RawGameData gameData) {
            return new CardData(
                    this.id,
                    gameData.getScript(this.drawScript),
                    gameData.getScript(this.playScript)
            );
        }

        public CardInitData toInitData() {
            return new CardInitData(this.id, toProperties(properties));
        }
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RawScript {
        @XmlValue
        private String script;
        @XmlAttribute(name = "suppress-compression")
        private boolean suppressCompression;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RawScriptOrReference extends RawScript {
        @XmlAttribute(name = "ref")
        private String referenceId;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RawLibraryScript extends RawScript {
        @XmlAttribute(name = "id")
        private String scriptId;
    }

    public static final String ELEM_SCRIPT_LANGUAGE = "ScriptLanguage";
    public static final String ELEM_PLAYER_JOIN_SCRIPT = "PlayerJoinScript";
    public static final String ELEM_INIT_SCRIPT = "InitScript";

    @XmlAttribute(name = "id")
    private String id;
    @XmlAttribute(name = "title")
    private String title;
    @XmlAttribute(name = "blurb")
    private String blurb;
    @XmlAnyElement
    private List<Element> description;
    @XmlElement(name = ELEM_SCRIPT_LANGUAGE)
    private ScriptingLanguage scriptingLanguage;
    @XmlElement(name = ELEM_PLAYER_JOIN_SCRIPT)
    private RawScriptOrReference playerJoinScript;
    @XmlElement(name = ELEM_INIT_SCRIPT)
    private RawScriptOrReference initScript;
    @XmlElement(name = "LibraryScript")
    private List<RawLibraryScript> libraryScripts;
    @XmlElement(name = "Property")
    private List<RawProperty> properties;
    @XmlElement(name = "Deck")
    private List<RawDeckData> decks;
    @XmlElement(name = "Card")
    private List<RawCardData> cards;
    @XmlAttribute(name = "qr-code-path")
    private String qrCodePath;
    @XmlAttribute(name = "qr-code-width")
    private Integer qrCodeWidth;
    @XmlAttribute(name = "qr-code-height")
    private Integer qrCodeHeight;

    public RawGameData() {
        this.properties = new ArrayList<RawProperty>();
        this.libraryScripts = new ArrayList<RawLibraryScript>();
        this.decks = new ArrayList<RawDeckData>();
        this.cards = new ArrayList<RawCardData>();
    }

    public String getScript(RawScriptOrReference scriptOrReference) {
        String script;
        if( scriptOrReference != null ) {
            if( scriptOrReference.getReferenceId() != null ) {
                script = this.getLibraryScript(scriptOrReference.getReferenceId());
            } else {
                script = scriptOrReference.getScript();
            }
        } else {
            script = null;
        }
        return script;
    }

    public String getLibraryScript(String referenceId) {
        for( RawLibraryScript libraryScript : this.getLibraryScripts() ) {
            if( libraryScript.getScriptId().equals(referenceId) ) {
                return libraryScript.getScript();
            }
        }
        return null;
    }

    public GameData toData() {
        GameData gameData = new GameData(
                this.scriptingLanguage,
                this.id,
                this.getScript(this.playerJoinScript),
                this.getScript(this.initScript),
                this.toInitData()
        );
        return gameData;
    }

    public GameInitData toInitData() {
        ArrayList<CardInitData> initCards = new ArrayList<CardInitData>(this.cards.size());
        for( RawCardData rawCardData : this.cards ) {
            initCards.add(rawCardData.toInitData());
        }

        ArrayList<DeckInitData> initDecks = new ArrayList<DeckInitData>(this.decks.size());
        for( RawDeckData rawDeckData : this.decks ) {
            initDecks.add(rawDeckData.toInitData());
        }

        Map<String, String> initProperties = toProperties(this.properties);

        return new GameInitData(initCards, initDecks, initProperties);
    }
}
