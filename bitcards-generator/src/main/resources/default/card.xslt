<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink= "http://www.w3.org/1999/xlink"
                xmlns="http://www.w3.org/2000/svg">

    <xsl:output method="xml"/>

    <xsl:param name="fill-color">#FFFFFF</xsl:param>
    <xsl:param name="card-width">190</xsl:param>
    <xsl:param name="card-height">260</xsl:param>

    <xsl:param name="title-height"><xsl:value-of select="$card-height div 10"/></xsl:param>
    <xsl:param name="description-height"><xsl:value-of select="$card-height div 20"/></xsl:param>
    <xsl:param name="card-margin"><xsl:value-of select="$card-height div 30"/></xsl:param>
    <xsl:param name="card-border"><xsl:value-of select="$card-height div 260"/></xsl:param>

    <xsl:template match="/">

        <svg width="{$card-width}" height="{$card-height}" viewBox="0 0 {$card-width} {$card-height}">
            <g>
                <rect
                        x="0"
                        y="0"
                        width="{$card-width}"
                        height="{$card-height}"
                        fill="white"/>
                <rect
                        x="{$card-margin}"
                        y="{$card-margin}"
                        width="{$card-width - $card-margin * 2}"
                        height="{$card-height - $card-margin * 2}"
                        rx="{$card-margin}"
                        ry="{$card-margin}"
                        fill="black"/>
                <rect
                        x="{$card-margin + $card-border}"
                        y="{$card-margin + $card-border}"
                        width="{$card-width - ($card-margin + $card-border) * 2}"
                        height="{$card-height - ($card-margin + $card-border) * 2}"
                        rx="{$card-margin - $card-border}"
                        ry="{$card-margin - $card-border}"
                        fill="white"/>
                <image
                        xlink:href="{Card/@qr-code-path}"
                        x="{($card-width - Card/@qr-code-width) div 2}"
                        y="{$title-height + $card-margin * 2 + $card-border}"
                        width="{Card/@qr-code-width}px"
                        height="{Card/@qr-code-height}px"/>
                <text
                        x="{$card-width div 2}"
                        y="{$card-margin * 2 + $title-height}"
                        font-size="{$title-height}"
                        text-anchor="middle"
                        font-weight="bold"
                        >
                    <!--textLength='{$card-width - $card-margin * 4 - $card-border * 2}' lengthAdjust="spacingAndGlyphs" -->
                    <xsl:value-of select="Card/@title"/>
                </text>
                <flowText font-size="{$description-height}" xmlns="http://xml.apache.org/batik/ext">
                    <flowRegion>
                        <xsl:variable name="y" select="$card-margin * 2 + $card-border + $title-height + Card/@qr-code-height"/>
                        <rect
                                x="{$card-margin * 2 + $card-border}"
                                y="{$y}"
                                width="{$card-width - $card-margin * 4 - $card-border * 2}"
                                height="{$card-height - $y - $card-margin - $card-border}"/>
                    </flowRegion>

                    <flowDiv>
                        <flowPara justification="full"><xsl:value-of select="Card/Description"/></flowPara>
                    </flowDiv>
                </flowText>
            </g>
        </svg>
    </xsl:template>

</xsl:stylesheet>