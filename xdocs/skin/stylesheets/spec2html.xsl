<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- ====================================================================== -->
<!-- inherit the document2html templates -->
<!-- ====================================================================== -->

 <xsl:import href="document2html.xsl"/>

<!-- ====================================================================== -->
<!-- header -->
<!-- ====================================================================== -->

 <xsl:template match="header">
  <div align="center">
   <table width="60%" border="0" cellspacing="2" cellpadding="2">
    <tr>
     <td bgcolor="#039acc" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
       <b>Authors</b>
      </font>
     </td>
    </tr>
    <xsl:for-each select="authors/person">
     <tr>
      <td bgcolor="#a0ddf0" valign="center" align="left">
       <font color="#000000" size="-1" face="arial,helvetica,sanserif">
        <b><xsl:value-of select="@name"/></b> - <xsl:value-of select="@email"/>
       </font>
      </td>
     </tr>
    </xsl:for-each>
    <tr>
     <td bgcolor="#039acc" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
       <b>Status</b>
      </font>
     </td>
    </tr>
    <tr>
     <td bgcolor="#a0ddf0" valign="center" align="left">
      <font color="#000000" size="-1" face="arial,helvetica,sanserif">
       <b><xsl:value-of select="type"/> - <xsl:value-of select="version"/></b>
      </font>
     </td>
    </tr>
    <tr>
     <td bgcolor="#039acc" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
       <b>Notice</b>
      </font>
     </td>
    </tr>
    <tr>
     <td bgcolor="#a0ddf0" valign="center" align="left">
      <font color="#000000" size="-1" face="arial,helvetica,sanserif">
       <xsl:value-of select="notice"/>
      </font>
     </td>
    </tr>
    <tr>
     <td bgcolor="#039acc" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
       <b>Abstract</b>
      </font>
     </td>
    </tr>
    <tr>
     <td bgcolor="#a0ddf0" valign="center" align="left">
      <font color="#000000" size="-1" face="arial,helvetica,sanserif">
       <xsl:value-of select="abstract"/>
      </font>
     </td>
    </tr>
   </table>
  </div>
  <br/>
 </xsl:template>

<!-- ====================================================================== -->
<!-- appendices section -->
<!-- ====================================================================== -->

 <xsl:template match="appendices">
  <xsl:apply-templates/>
 </xsl:template>

<!-- ====================================================================== -->
<!-- bibliography -->
<!-- ====================================================================== -->

 <xsl:template match="bl">
  <ul>
   <xsl:apply-templates/>
  </ul>
 </xsl:template>

 <xsl:template match="bi">
  <li>
   <b>
    <xsl:text>[</xsl:text>
     <a href="{@href}"><xsl:value-of select="@name"/></a>
    <xsl:text>]</xsl:text>
   </b>
   <xsl:text> &quot;</xsl:text>
   <xsl:value-of select="@title"/>
   <xsl:text>&quot;, </xsl:text>
   <xsl:value-of select="@authors"/>
   <xsl:if test="@date">
    <xsl:text>, </xsl:text>
    <xsl:value-of select="@date"/>
   </xsl:if>
  </li>
 </xsl:template>

</xsl:stylesheet>
