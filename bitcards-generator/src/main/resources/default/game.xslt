<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink= "http://www.w3.org/1999/xlink"
                xmlns="http://www.w3.org/2000/svg">

    <xsl:output method="xml"/>

    <xsl:include href="common.xslt"/>

    <xsl:param name="fill-color">#FFFFFF</xsl:param>
    <xsl:param name="page-width">595</xsl:param>
    <xsl:param name="page-height">842</xsl:param>
    <xsl:param name="page-margin"><xsl:value-of select="$page-height div 45"/></xsl:param>
    <xsl:param name="title-height"><xsl:value-of select="$page-height div 45"/></xsl:param>
    <xsl:param name="description-height"><xsl:value-of select="$page-height div 65"/></xsl:param>

    <xsl:template match="/">

        <svg width="{$page-width}" height="{$page-height}" viewBox="0 0 {$page-width} {$page-height}">
            <g>
                <rect
                        x="0"
                        y="0"
                        width="{$page-width}"
                        height="{$page-height}"
                        fill="white"/>
                <image
                        xlink:href="{Game/@qr-code-path}"
                        x="{$page-width - $page-margin - Game/@qr-code-width}"
                        y="{$page-margin}"
                        width="{Game/@qr-code-width}px"
                        height="{Game/@qr-code-height}px"/>
                <text
                        x="{$page-margin}"
                        y="{$page-margin + $title-height}"
                        font-size="{$title-height}"
                        font-weight="bold">
                    <xsl:value-of select="Game/@title"/>
                </text>
                <flowText xmlns="http://xml.apache.org/batik/ext">
                    <flowRegion>
                        <rect
                                x="{$page-margin}"
                                y="{$page-margin + $title-height + $description-height}"
                                width="{$page-width - Game/@qr-code-width - $page-margin * 3}"
                                height="{Game/@qr-code-height - $page-margin - $title-height}"/>
                    </flowRegion>
                    <flowDiv>
                        <flowPara font-size="{$description-height}" font-style="italic">
                            <xsl:value-of select="Game/@blurb"/>
                        </flowPara>
                    </flowDiv>

                </flowText>
                <flowText xmlns="http://xml.apache.org/batik/ext">
                    <flowRegion>
                        <rect
                            x="{$page-margin}"
                            y="{Game/@qr-code-height + $page-margin + $description-height}"
                            width="{$page-width - $page-margin * 2}"
                            height="{$page-height - Game/@qr-code-height - $page-margin * 2}"/>

                    </flowRegion>

                    <flowDiv>
                        <xsl:apply-templates select="Game/Description">
                            <xsl:with-param name="font-size" select="$description-height"/>
                        </xsl:apply-templates>

                        <!-- common instructions -->
                        <flowPara  font-size="{$title-height}" font-weight="bold" bottom-margin="{$description-height}">
                            Instructions
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            Every player should download the bitcards app and install it on their phone.
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            One player should hotspot their phone (the app does not require Internet if you want to disable data). Everyone else should connect to the hotspotted network.
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            Everyone should then start the app and lay their phone down flat (face up).
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            One player should scan the QR code at the top of this page. Scanning the QR code again will restart the game. You scan a code by holding it steady facing the camera. Once successful you will see a long red flash.
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            Once you are connected and the game is started, your phone will flash instructions to you.
                        </flowPara>
                        <flowPara  font-size="{$title-height}" font-weight="bold" bottom-margin="{$description-height}">
                            Sequences
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            <flowSpan font-weight="bold">RRRR</flowSpan> - Hold, do nothing. An all red flash of approximately 2 seconds in duration will indicate a successful scan and will preceed any changes in state
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            <flowSpan font-weight="bold">GGGG</flowSpan> - Draw, pick up a card and scan it
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            <flowSpan font-weight="bold">RGRG</flowSpan> - Play, play a card from your hand, make sure to scan it before you play it
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            <flowSpan font-weight="bold">RRRG</flowSpan> - No connection, you need to connect to a hot-spotted phone running the app
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            <flowSpan font-weight="bold">GGGR</flowSpan> - No Game, you need to scan the QR code above
                        </flowPara>
                        <flowPara  font-size="{$description-height}" bottom-margin="{$description-height}">
                            <flowSpan font-weight="bold">Flashing Quickly</flowSpan> - User Error, you made a mistake, undo whatever you just did
                        </flowPara>

                    </flowDiv>

                </flowText>

            </g>
        </svg>
    </xsl:template>

</xsl:stylesheet>