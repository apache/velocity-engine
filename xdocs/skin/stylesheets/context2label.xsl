<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="image"/>
  <xsl:param name="color"/>

  <xsl:template match="context">
    <image source="{$image}">
      <xsl:apply-templates/>
    </image>
  </xsl:template>

  <xsl:template match="parameter">
    
    <xsl:if test="@name='label'">
      <text font="officina" size="12" x="14" y="1" halign="left"
            valign="top" color="{$color}" style="italic" text="{@value}"/>
    </xsl:if>
  
  </xsl:template>

</xsl:stylesheet>
