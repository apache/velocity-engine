<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <project>
      <xsl:apply-templates/>
    </project>
  </xsl:template>

  <xsl:template match="img|figure|icon">
    <resource source="{@src}" target="{@src}"/>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>