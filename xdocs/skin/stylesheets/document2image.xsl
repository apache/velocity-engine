<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="label"/>

  <xsl:template match="/">
    <xsl:variable name="label">
      <xsl:if test="//header/title">
        <xsl:value-of select="//header/title"/>
      </xsl:if>
    </xsl:variable>
    
      <image width="1" height="35" bgcolor="ffffff">
      <text font="courier" size="1" x="1" y="1" halign="right" valign="top" color="000000" text="{$label}"/>
      </image>
       
  </xsl:template>

</xsl:stylesheet>
