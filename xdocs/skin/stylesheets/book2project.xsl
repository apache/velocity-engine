<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- match the root book element -->
  <xsl:template match="book">
    <project>

      <parameter name="copyright" value="{@copyright}"/>
      <parameter name="name" value="{@software}"/>

      <!-- copy all resources to the targets -->
      <process source="sbk:/style/resources/" producer="directory">
        <processor name="xslt">
          <parameter name="stylesheet" value="sbk:/style/stylesheets/directory2project.xsl"/>
          <parameter name="base" value="resources/"/>
        </processor>
      </process>

      <xsl:apply-templates/>

    </project>
  </xsl:template>

<!-- ********************************************************************** -->
<!-- CREATE THE TARGET HTML -->
<!-- ********************************************************************** -->

  <xsl:template match="page|hidden">
    <process source="{@source}" producer="parser">
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/scan4resources.xsl"/>
      </processor>
    </process>

    <xsl:call-template name="header">
      <xsl:with-param name="id"     select="@id"/>
      <xsl:with-param name="source" select="@source"/>
      <xsl:with-param name="label"  select="@label"/>
    </xsl:call-template>

    <xsl:if test="not(name(.)='hidden')">
      <xsl:call-template name="labels">
        <xsl:with-param name="id" select="@id"/>
        <xsl:with-param name="label" select="@label"/>
      </xsl:call-template>
    </xsl:if>
    
    <create source="{@source}" target="{@id}.html" producer="parser" printer="html">
      <processor name="xslt">
        <parameter name="id" value="{@id}"/>
        <parameter name="stylesheet" value="sbk:/style/stylesheets/document2html.xsl"/>
      </processor>
    </create>
  </xsl:template>

  <xsl:template match="spec">
    <process source="{@source}" producer="parser">
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/scan4resources.xsl"/>
      </processor>
    </process>

    <xsl:call-template name="header">
      <xsl:with-param name="id"     select="@id"/>
      <xsl:with-param name="source" select="@source"/>
      <xsl:with-param name="label"  select="@label"/>
    </xsl:call-template>

    <xsl:call-template name="labels">
      <xsl:with-param name="id" select="@id"/>
      <xsl:with-param name="label" select="@label"/>
    </xsl:call-template>
    
    <create source="{@source}" target="{@id}.html" producer="parser" printer="html">
      <processor name="xslt">
        <parameter name="id" value="{@id}"/>
        <parameter name="stylesheet" value="sbk:/style/stylesheets/spec2html.xsl"/>
      </processor>
    </create>
  </xsl:template>
  
  <xsl:template match="changes|faqs|todo">
    <process source="{@source}" producer="parser">
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/{name(.)}2document.xsl"/>
      </processor>
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/scan4resources.xsl"/>
      </processor>
    </process>

    <xsl:call-template name="header">
      <xsl:with-param name="id"     select="@id"/>
      <xsl:with-param name="source" select="@source"/>
      <xsl:with-param name="label"  select="@label"/>
      <xsl:with-param name="type"   select="name(.)"/>
    </xsl:call-template>

    <xsl:call-template name="labels">
      <xsl:with-param name="id" select="@id"/>
      <xsl:with-param name="label" select="@label"/>
    </xsl:call-template>

    <create source="{@source}" target="{@id}.html" producer="parser" printer="html">
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/{name(.)}2document.xsl"/>
      </processor>
      <processor name="xslt">
        <parameter name="id" value="{@id}"/>
        <parameter name="stylesheet" value="sbk:/style/stylesheets/document2html.xsl"/>
      </processor>
    </create>
  </xsl:template>
  
  <xsl:template match="external">
    <xsl:call-template name="labels">
      <xsl:with-param name="id" select="concat('ext-',position())"/>
      <xsl:with-param name="label" select="@label"/>
    </xsl:call-template>
  </xsl:template>
  
<!-- ********************************************************************** -->
<!-- NAMED TEMPLATES -->
<!-- ********************************************************************** -->

<!-- Generate the doument header image -->
  <xsl:template name="header"> 
    <xsl:param name="id"/>
    <xsl:param name="source"/>
    <xsl:param name="label"/>
    <xsl:param name="type"/>

    <create source="{$source}" target="graphics/{$id}-header.jpg" producer="parser" printer="image">
      <xsl:if test="$type">
       <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/{$type}2document.xsl"/>
       </processor>
      </xsl:if>
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/document2image.xsl"/>
      </processor>
    </create>
  </xsl:template>

<!-- Generate the three rollover label images -->
  <xsl:template name="labels"> 
    <xsl:param name="id"/>
    <xsl:param name="label"/>
    
    <create source="" target="graphics/{$id}-label-1.jpg" producer="context" printer="image">
      <parameter name="label" value="{$label}"/>
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/context2label.xsl"/>
        <parameter name="image" value="sbk:/style/graphics/button-a.gif"/>
        <parameter name="color" value="ffffff"/>
      </processor>
    </create>

    <create source="" target="graphics/{$id}-label-2.jpg" producer="context" printer="image">
      <parameter name="label" value="{$label}"/>
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/context2label.xsl"/>
        <parameter name="image" value="sbk:/style/graphics/button-b.gif"/>
        <parameter name="color" value="ffff00"/>
      </processor>
    </create>

    <create source="" target="graphics/{$id}-label-3.jpg" producer="context" printer="image">
      <parameter name="label" value="{$label}"/>
      <processor name="xslt">
        <parameter name="stylesheet" value="sbk:/style/stylesheets/context2label.xsl"/>
        <parameter name="image" value="sbk:/style/graphics/button-b.gif"/>
        <parameter name="color" value="ffffff"/>
      </processor>
    </create>
  </xsl:template>

</xsl:stylesheet>