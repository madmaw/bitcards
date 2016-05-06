package com.darklanders.bitcards.generator;

import com.darklanders.bitcards.common.CardData;
import com.darklanders.bitcards.common.GameData;
import com.darklanders.bitcards.common.GameInitData;
import com.darklanders.bitcards.common.IOHelper;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * Created by Chris on 28/04/2016.
 */
public class RawGameDataQRCodeGenerator {

    public static final void generateQRCodes(
            File outputDir,
            RawGameData rawGameData,
            int maxGameWidth,
            int maxGameHeight,
            int maxCardWidth,
            int maxCardHeight
    ) throws Exception {
        // generate the game QR code
        GameData gameData = rawGameData.toData();
        StringWriter gameWriter = new StringWriter();
        IOHelper.BitWriter gameBitWriter = new IOHelper.BitWriter(gameWriter);
        gameBitWriter.writeGameData(gameData);

        String gameFilename = rawGameData.getQrCodePath();
        if( gameFilename == null ) {
            gameFilename = "qr/game.png";
            rawGameData.setQrCodePath(gameFilename);
        }
        File gameFile = new File(outputDir, gameFilename);
        File qrDirectory = new File(outputDir, "qr");
        qrDirectory.mkdirs();
        OutputStream gameOuts = new FileOutputStream(gameFile);
        try {
            // TODO svg!!
            QRCode.from(gameWriter.toString()).to(ImageType.PNG).withSize(maxGameWidth, maxGameHeight).withCharset("UTF-8").writeTo(gameOuts);
        } finally {
            IOUtils.closeQuietly(gameOuts);
        }
        BufferedImage image = ImageIO.read(gameFile);
        rawGameData.setQrCodeWidth(image.getWidth());
        rawGameData.setQrCodeHeight(image.getHeight());

        // generate the card QR codes
        for(RawGameData.RawCardData rawCardData : rawGameData.getCards()) {
            CardData cardData = rawCardData.toData(rawGameData);
            StringWriter cardWriter = new StringWriter();
            IOHelper.BitWriter cardBitWriter = new IOHelper.BitWriter(cardWriter);
            cardBitWriter.writeCardData(cardData);

            String cardFilename = rawCardData.getQrCodePath();
            if( cardFilename == null ) {
                cardFilename = "qr/"+cardData.getId()+".png";
                rawCardData.setQrCodePath(cardFilename);
            }
            File cardFile = new File(outputDir, cardFilename);
            OutputStream cardOuts = new FileOutputStream(cardFile);
            try {
                QRCode.from(cardWriter.toString()).to(ImageType.PNG).withSize(maxCardWidth, maxCardHeight).withCharset("UTF-8").writeTo(cardOuts);
            } finally {
                IOUtils.closeQuietly(cardOuts);
            }
            BufferedImage cardImage = ImageIO.read(cardFile);
            rawCardData.setQrCodeWidth(cardImage.getWidth());
            rawCardData.setQrCodeHeight(cardImage.getHeight());

        }


    }

}
