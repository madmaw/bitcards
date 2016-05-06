package com.darklanders.bitcards.common;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * Created by Chris on 27/04/2016.
 */
public class IOHelper {

    private static final int VALUE_MASK = 1+2+4+8+16;
    private static final int CONTINUE_MASK = 32;
    private static final int SHIFT_AMOUNT = 5;

    private static final char CARD_MARKER = 'c';
    private static final char GAME_MARKER = 'g';

    private static final char[] ORDER = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '+', '/'
    };

    private static final Map<Character, Integer> INDEX = new HashMap<Character, Integer>();

    static {
        for( int i=0; i<ORDER.length; i++ ) {
            Character c = ORDER[i];
            INDEX.put(c, i);
        }
    }

    public static class PropertiesIndex {
        private List<String> keys;

        public PropertiesIndex() {
            this.keys = new ArrayList<String>();
        }

        public PropertiesIndex(List<String> keys) {
            this.keys = keys;
        }

        public void addKey(String key) {
            if( !this.keys.contains(key) ) {
                this.keys.add(key);
            }
        }

        public void addKeys(Collection<String> keys) {
            for( String key : keys ) {
                this.addKey(key);
            }
        }

        public List<String> getKeys() {
            return this.keys;
        }

        public int getKeyIndex(String key) {
            return this.keys.indexOf(key);
        }

        public String getKey(int index) {
            return this.keys.get(index);
        }
    }

    public static class BitReader {
        private Reader reader;
        private int reads;
        private String original;

        public BitReader(Reader reader, String original) {
            this.reader = reader;
            this.reads = 0;
            this.original = original;
        }

        private void eof() throws EOFException {
            throw new EOFException("at "+Integer.toString(reads)+" in "+this.original);
        }

        public int readInt() throws IOException {
            boolean more;
            int result = 0;
            int shift = 0;
            do {
                int c = reader.read();
                if( c >= 0 ) {
                    this.reads++;
                    Character key = new Character((char)c);
                    if( INDEX.containsKey(key) ) {
                        int val = INDEX.get(new Character((char)c));
                        int actualVal = VALUE_MASK & val;
                        more = (val & CONTINUE_MASK) > 0;
                        actualVal <<= shift;
                        shift += SHIFT_AMOUNT;
                        result |= actualVal;
                    } else {
                        throw new IOException("unexpected character "+key+" at position "+this.reads+" in "+this.original);
                    }
                } else {
                    this.eof();
                    more = false;
                }
            } while(more);
            return result;
        }

        public String readString() throws IOException {
            int length = readInt();
            StringBuffer sb = new StringBuffer(length);
            while( length > 0 ) {
                length--;
                int c = this.reader.read();
                if( c >= 0 ) {
                    this.reads++;
                    sb.append((char)c);
                } else {
                    this.eof();
                }
            }
            return sb.toString();
        }

        public boolean readBoolean() throws IOException {
            return readInt() != 0;
        }

        public PropertiesIndex readPropertiesIndex() throws IOException {
            int length = readInt();
            ArrayList<String> keys = new ArrayList<String>(length);
            for( int i=0; i<length; i++ ) {
                String key = readString();
                keys.add(key);
            }
            return new PropertiesIndex(keys);
        }

        public Map<String, String> readProperties(PropertiesIndex index) throws IOException {
            int size = readInt();
            HashMap<String, String> properties = new HashMap<String, String>(size);
            for( int i=0; i<size; i++ ) {
                int keyIndex = readInt();
                String value = readString();
                String key = index.getKey(keyIndex);
                properties.put(key, value);
            }
            return properties;
        }

        // generic read
        public Object read() throws IOException {
            Object result;
            int c = this.reader.read();
            this.reads++;
            switch(c) {
                case CARD_MARKER:
                    result = this.readCardDataNoMarker();
                    break;
                case GAME_MARKER:
                    result = this.readGameDataNoMarker();
                    break;
                default:
                    throw new IOException("unexpected marker "+((char)c)+" in "+this.original);
            }
            return result;
        }

        // cards
        public CardData readCardDataNoMarker() throws IOException {
            String cardId = this.readString();
            String drawScript = this.readString();
            String playScript = this.readString();
            return new CardData(cardId, drawScript, playScript);
        }

        // game data
        public GameData readGameDataNoMarker() throws IOException {
            int version = readInt();
            if( version != 1 ) {
                throw new IOException("unrecognised version number "+version);
            }

            String gameId = readString();

            int scriptLanguageCode = (char)this.reader.read();
            this.reads++;
            ScriptingLanguage scriptLanguage = ScriptingLanguage.getForShortCode((char)scriptLanguageCode);
            if( scriptLanguage == null ) {
                throw new IOException("unknown scripting language code "+scriptLanguageCode+" ("+(char)scriptLanguageCode+") at "+this.reads+" in "+this.original);
            }
            String onPlayerJoinScript = readString();
            String initScript = readString();
            GameInitData initData = readGameInitData();
            return new GameData(scriptLanguage, gameId, onPlayerJoinScript, initScript, initData);
        }

        public GameInitData readGameInitData() throws IOException {
            boolean isNotNull = readBoolean();
            if( isNotNull ) {
                PropertiesIndex index = readPropertiesIndex();
                List<CardInitData> cards = readCardInitDataList(index);
                List<DeckInitData> decks = readDeckInitDataList(index);
                Map<String, String> properties = readProperties(index);
                return new GameInitData(cards, decks, properties);
            } else {
                return null;
            }
        }

        public List<CardInitData> readCardInitDataList(PropertiesIndex index) throws IOException {
            int length = readInt();
            ArrayList<CardInitData> cardInitDataList = new ArrayList<CardInitData>(length);
            for( int i=0; i<length; i++ ) {
                CardInitData cardInitData = readCardInitData(index);
                cardInitDataList.add(cardInitData);
            }
            return cardInitDataList;
        }

        public CardInitData readCardInitData(PropertiesIndex index) throws IOException {
            String cardId = readString();
            Map<String, String> properties = readProperties(index);
            return new CardInitData(cardId, properties);
        }

        public List<DeckInitData> readDeckInitDataList(PropertiesIndex index) throws IOException {
            int length = readInt();
            ArrayList<DeckInitData> deckInitDataList = new ArrayList<DeckInitData>(length);
            for( int i=0; i<length; i++ ) {
                DeckInitData deckInitData = readDeckInitData(index);
                deckInitDataList.add(deckInitData);
            }
            return deckInitDataList;
        }

        public DeckInitData readDeckInitData(PropertiesIndex index) throws IOException {
            String deckId = readString();
            Map<String, String> properties = readProperties(index);
            return new DeckInitData(deckId, properties);
        }
    }

    public static class BitWriter {
        private Writer writer;

        public BitWriter(Writer writer) {
            this.writer = writer;
        }

        // common
        public void writeInt(int value) throws IOException {
            do {
                int index = value & VALUE_MASK;
                value >>= SHIFT_AMOUNT;
                if( value > 0 ) {
                    index |= CONTINUE_MASK;
                }
                char c = ORDER[index];
                writer.write(c);
            } while( value > 0 );
        }

        public void writeString(String value) throws IOException {
            if( value != null ) {
                this.writeInt(value.length());
                this.writer.write(value);
            } else {
                this.writeInt(0);
            }
        }

        public void writeBoolean(boolean b) throws IOException {
            writeInt(b?1:0);
        }

        public PropertiesIndex writePropertiesIndex(Set<String>... keys) throws IOException {
            HashSet<String> allKeys = new HashSet<String>();
            for( Set<String> i : keys ) {
                allKeys.addAll(i);
            }
            PropertiesIndex propertiesIndex = new PropertiesIndex();
            propertiesIndex.addKeys(allKeys);
            writePropertiesIndex(propertiesIndex);
            return propertiesIndex;
        }

        public void writePropertiesIndex(PropertiesIndex propertiesIndex) throws IOException {
            List<String> keys = propertiesIndex.getKeys();
            this.writeInt(keys.size());
            for( String key : keys ) {
                this.writeString(key);
            }
        }

        public void writeProperties(Map<String, String> properties, PropertiesIndex propertiesIndex) throws IOException {
            if( properties != null ) {
                this.writeInt(0);
            } else {
                this.writeInt(properties.size());
                for( Map.Entry<String, String> entry : properties.entrySet() ) {
                    this.writeInt(propertiesIndex.getKeyIndex(entry.getKey()));
                    this.writeString(entry.getValue());
                }
            }
        }

        // card data
        public void writeCardData(CardData cardData) throws IOException {
            this.writer.write(CARD_MARKER);
            this.writeString(cardData.getId());
            this.writeString(cardData.getOnDrawScript());
            this.writeString(cardData.getOnPlayScript());

        }

        // game data
        public void writeGameData(GameData gameData) throws IOException {
            this.writer.write(GAME_MARKER);
            // version
            this.writeInt(1);
            this.writeString(gameData.getId());
            this.writer.write(gameData.getScriptLanguage().getShortCode());
            this.writeString(gameData.getOnPlayerJoinScript());
            this.writeString(gameData.getInitScript());
            this.writeGameInitData(gameData.getInitData());
        }

        public void writeGameInitData(GameInitData gameInitData) throws IOException {
            if( gameInitData != null ) {

                this.writeBoolean(true);

                Set<String> cardKeys = new HashSet<String>();
                Set<String> deckKeys = new HashSet<String>();

                for( CardInitData cardInitData : gameInitData.getCards() ) {
                    cardKeys.addAll(cardInitData.getProperties().keySet());
                }
                for( DeckInitData deckInitData : gameInitData.getDecks() ) {
                    deckKeys.addAll(deckInitData.getProperties().keySet());
                }

                PropertiesIndex propertiesIndex = this.writePropertiesIndex(gameInitData.getProperties().keySet(), cardKeys, deckKeys);
                this.writeCardInitDataList(gameInitData.getCards(), propertiesIndex);
                this.writeDeckInitDataList(gameInitData.getDecks(), propertiesIndex);
                this.writeProperties(gameInitData.getProperties(), propertiesIndex);

            } else {
                this.writeBoolean(false);
            }
        }

        public void writeCardInitDataList(List<CardInitData> cardInitData, PropertiesIndex index) throws IOException {
            this.writeInt(cardInitData.size());
            for( CardInitData i : cardInitData ) {
                writeCardInitData(i, index);
            }
        }

        public void writeCardInitData(CardInitData cardInitData, PropertiesIndex index) throws IOException {
            writeString(cardInitData.getId());
            writeProperties(cardInitData.getProperties(), index);
        }

        public void writeDeckInitDataList(List<DeckInitData> deckInitData, PropertiesIndex index) throws IOException {
            this.writeInt(deckInitData.size());
            for( DeckInitData i : deckInitData ) {
                writeDeckInitData(i, index);
            }
        }

        public void writeDeckInitData(DeckInitData deckInitData, PropertiesIndex index) throws IOException {
            writeString(deckInitData.getId());
            writeProperties(deckInitData.getProperties(), index);
        }
    }


}
