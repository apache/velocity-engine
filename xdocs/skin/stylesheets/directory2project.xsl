<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="base"/>

  <xsl:template match="directory">
    <project>
      <xsl:apply-templates/>
    </project>
  </xsl:template>

  <xsl:template match="entry">
    <xsl:if test="not(@directory)">
      <resource source="{@href}" target="{$base}{@href}"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>