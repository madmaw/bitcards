package com.darklanders.bitcards.generator;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Chris on 28/04/2016.
 */
public class RawGameDataRenderer {

    public static final String PARAM_PAGE_WIDTH = "page-width";
    public static final String PARAM_PAGE_HEIGHT = "page-height";
    public static final String PARAM_CARD_WIDTH = "card-width";
    public static final String PARAM_CARD_HEIGHT = "card-height";

    private static RenderingHints RENDERING_HINTS = new RenderingHints(new HashMap<RenderingHints.Key, Object>());

    static {
        RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RENDERING_HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        RENDERING_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        RENDERING_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        RENDERING_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }


    public static final void renderGame(
            File outDirectory,
            RawGameData gameData,
            String style,
            int pageWidth,
            int pageHeight
    ) throws Exception {
        File outFile = new File(outDirectory, "game.png");

        JAXBContext jaxbContext = JAXBContext.newInstance(RawGameData.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter xmlWriter = new StringWriter();
        marshaller.marshal(gameData, xmlWriter);
        String xml = xmlWriter.toString();

        System.out.println(xml);

        String xslPath = "/"+style+"/game.xslt";

        StreamSource xslSource = new StreamSource(
                RawGameDataRenderer.class.getResourceAsStream(xslPath),
                RawGameDataRenderer.class.getResource(xslPath).toExternalForm()
        );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslSource);
        transformer.setParameter(PARAM_PAGE_WIDTH, pageWidth);
        transformer.setParameter(PARAM_PAGE_HEIGHT, pageHeight);

        StreamSource xmlSource = new StreamSource(new StringReader(xml));

        StringWriter svgWriter = new StringWriter();
        StreamResult svgTarget = new StreamResult(svgWriter);

        transformer.transform(xmlSource, svgTarget);

        String svg = svgWriter.toString();

        System.out.println(svg);


        // write the SVG out to a file
        BufferedImage gameImage = writeSVGToImage(
                outFile.toURI().toString(),
                svg,
                pageWidth,
                pageHeight
        );
        FileOutputStream outs = new FileOutputStream(outFile);
        try {
            ImageIO.write(gameImage, "png", outs);
        } finally {
            IOUtils.closeQuietly(outs);
        }

    }

    public static final void renderCards(
            File outDirectory,
            RawGameData gameData,
            String style,
            int cardWidth,
            int cardHeight,
            int rows,
            int columns
    ) throws Exception {
        int cardsPerPage = rows * columns;
        BufferedImage bufferedImage = null;
        java.util.List<RawGameData.RawCardData> cards = gameData.getCards();
        for( int i=0; i<cards.size(); i++ ) {
            RawGameData.RawCardData card = cards.get(i);
            BufferedImage cardImage = renderCard(outDirectory.toURI().toString(), card, style, cardWidth, cardHeight);
            if( bufferedImage == null ) {
                bufferedImage = new BufferedImage(cardWidth * columns, cardHeight * rows, BufferedImage.TYPE_INT_ARGB);
            }
            int index = i % cardsPerPage;
            int row = index / columns;
            int column = index % columns;
            bufferedImage.getGraphics().drawImage(cardImage, column * cardWidth, row * cardHeight, null);
            if( i == cards.size() - 1 || i % cardsPerPage == cardsPerPage - 1) {
                // write out the file
                File file = new File(outDirectory, "card"+((i / cardsPerPage)+1)+".png");
                ImageIO.write(bufferedImage, "png", file);
                bufferedImage = null;
            }
        }
    }

    public static final BufferedImage renderCard(String outDirPath, RawGameData.RawCardData card, String style, int cardWidth, int cardHeight) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(RawGameData.RawCardData.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter xmlWriter = new StringWriter();
        marshaller.marshal(card, xmlWriter);

        String xslPath = "/"+style+"/card.xslt";

        StreamSource xslSource = new StreamSource(
                RawGameDataRenderer.class.getResourceAsStream(xslPath),
                RawGameDataRenderer.class.getResource(xslPath).toExternalForm()
        );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslSource);
        transformer.setParameter(PARAM_CARD_WIDTH, cardWidth);
        transformer.setParameter(PARAM_CARD_HEIGHT, cardHeight);

        StreamSource xmlSource = new StreamSource(new StringReader(xmlWriter.toString()));

        StringWriter svgWriter = new StringWriter();
        StreamResult svgTarget = new StreamResult(svgWriter);

        transformer.transform(xmlSource, svgTarget);

        String svg = svgWriter.toString();
        System.out.println(svg);

        // write the SVG out to a file
        BufferedImage cardImage = writeSVGToImage(
                outDirPath,
                svg,
                cardWidth,
                cardHeight
        );
        return cardImage;
    }

    public static final BufferedImage writeSVGToImage(String docBaseUri, String svg, int width, int height) throws Exception {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        SVGDocument svgDocument;
        try {
            svgDocument = f.createSVGDocument(docBaseUri, new StringReader(svg));
        } catch( Exception ex ) {
            throw new Exception( "invalid SVG \n"+svg, ex );
        }

        UserAgentAdapter userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        GraphicsNode rootGN;
        try {
            rootGN = builder.build(ctx, svgDocument);
            rootGN.setRenderingHints(RENDERING_HINTS);
        } catch( Exception ex ) {
            throw new Exception( "invalid SVG \n"+svg, ex );
        }


        SVGOMSVGElement element = (SVGOMSVGElement)svgDocument.getDocumentElement();
        int svgWidth = (int)Math.ceil(element.getWidth().getBaseVal().getValueInSpecifiedUnits());
        int svgHeight = (int)Math.ceil(element.getHeight().getBaseVal().getValueInSpecifiedUnits());
        int scaledWidth;
        int scaledHeight;
        if( svgWidth > 0 && svgHeight > 0 ) {
            // TODO scale to fit
            scaledWidth = svgWidth;
            scaledHeight = svgHeight;
        } else {
            scaledWidth = width;
            scaledHeight = height;
        }
        if( width > 0 && height > 0 ) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = GraphicsUtil.createGraphics(image);

            g.addRenderingHints(RENDERING_HINTS);
            rootGN.paint(g);
            g.dispose();

            if( scaledWidth > 0 && scaledHeight > 0 ) {
                if (scaledWidth != width || scaledHeight != height) {
                    Image toWrite = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                    image.getGraphics().drawImage(toWrite, 0, 0, null);
                }

                return image;
            }
        }
        return null;
    }
}
