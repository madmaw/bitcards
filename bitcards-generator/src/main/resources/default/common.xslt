<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://xml.apache.org/batik/ext">

    <xsl:output method="xml"/>

    <xsl:template match="Description">
        <xsl:param name="font-size">16</xsl:param>
        <xsl:apply-templates select="*" mode="desc">
            <xsl:with-param name="font-size" select="$font-size"/>
        </xsl:apply-templates>

    </xsl:template>

    <xsl:template match="p" mode="desc">
        <xsl:param name="font-size"/>

        <flowPara justification="full" font-size="{$font-size}" bottom-margin="{$font-size}">
            <xsl:value-of select="."/>
        </flowPara>
    </xsl:template>

    <xsl:template match="ol" mode="desc">
        <xsl:param name="font-size"/>
        <xsl:for-each select="li">
            <flowPara justification="left" font-size="{$font-size}" left-margin="{$font-size}">
                <xsl:attribute name="bottom-margin">
                    <xsl:choose>
                        <xsl:when test="position() = last()">
                            <xsl:value-of select="$font-size"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$font-size div 3"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <flowSpan><xsl:value-of select="position()"/>. </flowSpan><xsl:value-of select="."/>
            </flowPara>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>