package com.darklanders.bitcards.generator;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.lexicalscope.jewel.cli.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;

/**
 * Created by Chris on 28/04/2016.
 */
public class Main {

    private static interface Arguments {
        @Unparsed(name = "FILE", exactly = 1)
        File getInFile();

        @Option(shortName = "out", description = "directory to send output", defaultToNull = true)
        File getOutFile();

        @Option(helpRequest = true)
        boolean isHelp();

        @Option(shortName = "rows", description = "number of cards to have vertically on a page", defaultValue = "3")
        int getRowsPerPage();

        @Option(shortName = "cols", description = "number of cards to have horizontally on a page", defaultValue = "3")
        int getColumnsPerPage();

        @Option(shortName = "width", description = "width of a page", defaultValue = "2480")
        int getPageWidth();

        @Option(shortName = "height", description = "height of a page", defaultValue = "3508")
        int getPageHeight();

        @Option(shortName = "style", description = "style to use for rendering", defaultValue = "default")
        String getStyle();
    }

    public static final void main(String[] args) throws Exception {

        File inFile;
        File outFile;
        int pageWidth = 0;
        int pageHeight = 0;
        int rows = 1;
        int columns = 1;
        String style = null;

        Cli<Arguments> cli = CliFactory.createCli(Arguments.class);
        try {
            Arguments arguments = CliFactory.parseArguments(Arguments.class, args);
            if( arguments.isHelp() ) {
                inFile = null;
                outFile = null;
            } else {
                inFile = arguments.getInFile();
                outFile = arguments.getOutFile();
                if( outFile == null ) {
                    outFile = inFile.getParentFile();
                }
                pageWidth = arguments.getPageWidth();
                pageHeight = arguments.getPageHeight();
                rows = arguments.getRowsPerPage();
                columns = arguments.getColumnsPerPage();
                style = arguments.getStyle();
            }
        } catch( ArgumentValidationException ex ) {
            ex.printStackTrace();
            System.out.println(cli.getHelpMessage());
            inFile = null;
            outFile = null;
        }

        if( inFile != null && outFile != null ) {
            outFile.mkdirs();

            String xml = Files.toString(inFile, Charsets.UTF_8);

            JAXBContext jaxbContext = JAXBContext.newInstance(RawGameData.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            RawGameData rawGameData = (RawGameData)unmarshaller.unmarshal(reader);

            System.out.println(rawGameData);

            RawGameDataValidator.validate(rawGameData);

            RawGameDataCompressor.compressScripts(rawGameData);

            int cardWidth = pageWidth / columns;
            int cardHeight = pageHeight / rows;

            int cardQrCodeSize = (int)Math.floor(Math.min(cardWidth, cardHeight) * 0.75);
            int pageQrCodeSize = cardQrCodeSize;

            RawGameDataQRCodeGenerator.generateQRCodes(outFile, rawGameData, pageQrCodeSize, pageQrCodeSize, cardQrCodeSize, cardQrCodeSize);

            // generate game image
            RawGameDataRenderer.renderGame(outFile, rawGameData, style, pageWidth, pageHeight);

            // generate card image(s)
            RawGameDataRenderer.renderCards(outFile, rawGameData, style, cardWidth, cardHeight, rows, columns);
        }
    }


}
