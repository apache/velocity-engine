<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:param name="stylebook.project"/>
  <xsl:param name="copyright"/>
  <xsl:param name="id"/>

<!-- ====================================================================== -->
<!-- document section -->
<!-- ====================================================================== -->

 <xsl:template match="/">
  <!-- checks if this is the included document to avoid neverending loop -->
  <xsl:if test="not(book)">
    <html>
      <head>
        <title><xsl:value-of select="document/header/title"/></title>
      </head>
      <body text="#000000" link="#023264" vlink="#023264" alink="#023264"
            topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
            bgcolor="#ffffff">
        <!-- THE TOP BAR (HEADER) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td valign="top" align="left">
              <a href="http://jakarta.apache.org/index.html">
              <img src="resources/jakarta-logo.gif" hspace="0" vspace="0" border="0"/>
              </a>
            </td>

            <td width="100%" valign="top" align="left" bgcolor="#ffffff">
              <img src="resources/header.gif" hspace="0" vspace="0" border="0" alt="{header/title}" align="right"/>
            </td>
          </tr>
          
          <tr>
            <td>
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/>
            </td>
          </tr>
          
          <tr>
            <td width="100%" height="2" colspan="2" background="resources/line.gif">
                <img src="resources/line.gif" width="1" height="2" hspace="0" vspace="0" border="0" alt="{header/title}"/>
            </td>                
          </tr>
        </table>

        <!-- THE MAIN PANEL (SIDEBAR AND CONTENT) -->
        <table width="100%" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <!-- THE SIDE BAR -->
            <td width="1%" valign="top">
            </td>
            <td width="14%" valign="top" nowrap="1">
                <br/>
                <xsl:apply-templates select="document($stylebook.project)"/>
            </td>
            <!-- THE CONTENT PANEL -->
            <td width="*" valign="top" align="left">
              <table border="0" cellspacing="0" cellpadding="3">
                <tr><td><br/><xsl:apply-templates/></td></tr>
              </table>
            </td>
          </tr>
        </table>
        
        <br/>
        
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr><td bgcolor="#023264"><img src="resources/resources.gif" width="1" height="1"/></td></tr>
          <tr>
            <td align="center"><font face="arial,helvetica,sanserif" size="-1" color="#023264"><i>
              Copyright &#169; <xsl:value-of select="$copyright"/>.
              All Rights Reserved.
            </i></font></td>
          </tr>
        </table>
      </body>
    </html>
   </xsl:if>
   
   <xsl:if test="book">
    <xsl:apply-templates/>
   </xsl:if>
  </xsl:template>

<!-- ====================================================================== -->
<!-- book section -->
<!-- ====================================================================== -->

  <xsl:template match="page|faqs|changes|todo|spec">
    <font face="arial,helvetica,sanserif" size="-1">
    <li><a href="{@id}.html"><xsl:value-of select="@label"/></a></li>
    </font>
  </xsl:template>

  <xsl:template match="external">
    <li><a href="{@href}"><xsl:value-of select="@label"/></a></li>
    <br/>
  </xsl:template>

  <xsl:template match="separator">
    <p/>
  </xsl:template>

  <xsl:template match="section">
    <p><xsl:value-of select="@label"/></p>
  </xsl:template>
  
<!-- ====================================================================== -->
<!-- header section -->
<!-- ====================================================================== -->

 <xsl:template match="header">
  <!-- ignore on general document -->
 </xsl:template>

<!-- ====================================================================== -->
<!-- body section -->
<!-- ====================================================================== -->

  <xsl:template match="s1">
   <div align="right">
    <table border="0" width="98%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="100%" bgcolor="#023264">
          <font size="+1" face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/>
            <b><xsl:value-of select="@title"/></b></font>
         </td>
      </tr>
      <tr>
        <td>
          <img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/>
        </td>
      </tr>        
     </table>
     <table border="0" width="98%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

  <xsl:template match="s2">
   <div align="right">
    <table border="0" width="95%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="9" height="7" valign="bottom" align="right"><img src="resources/bar-top-left.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-top.gif"><img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="7" valign="bottom" align="left"><img src="resources/bar-top-right.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" background="resources/bar-border-left.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
        <td width="100%" bgcolor="#0086b2">
          <font face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/><b><xsl:value-of select="@title"/></b></font>
         </td>
        <td width="9" background="resources/bar-border-right.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" height="12" valign="top" align="right"><img src="resources/bar-bottom-left.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-bottom.gif"><img src="resources/void.gif" width="1" height="12" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="12" valign="top" align="left"><img src="resources/bar-bottom-right.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
      </tr>
     </table>
     <table border="0" width="95%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

  <xsl:template match="s3">
   <div align="right">
    <table border="0" width="90%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="9" height="7" valign="bottom" align="right"><img src="resources/bar-top-left.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-top.gif"><img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="7" valign="bottom" align="left"><img src="resources/bar-top-right.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" background="resources/bar-border-left.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
        <td width="100%" bgcolor="#0086b2">
          <font size="-1" face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/><b><xsl:value-of select="@title"/></b></font>
         </td>
        <td width="9" background="resources/bar-border-right.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" height="12" valign="top" align="right"><img src="resources/bar-bottom-left.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-bottom.gif"><img src="resources/void.gif" width="1" height="12" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="12" valign="top" align="left"><img src="resources/bar-bottom-right.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
      </tr>
     </table>
     <table border="0" width="90%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>

  <xsl:template match="s4">
   <div align="right">
    <table border="0" width="85%" cellspacing="0" cellpadding="0">
      <tr>
        <td width="9" height="7" valign="bottom" align="right"><img src="resources/bar-top-left.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-top.gif"><img src="resources/void.gif" width="1" height="5" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="7" valign="bottom" align="left"><img src="resources/bar-top-right.gif" width="9" height="7" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" background="resources/bar-border-left.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
        <td width="100%" bgcolor="#0086b2">
          <font size="-2" face="arial,helvetica,sanserif" color="#ffffff">
            <img src="resources/void.gif" width="5" height="5" vspace="0" hspace="0" border="0"/><b><xsl:value-of select="@title"/></b></font>
         </td>
        <td width="9" background="resources/bar-border-right.gif"><img src="resources/void.gif" width="9" height="1" vspace="0" hspace="0" border="0"/></td>
      </tr>
      <tr>
        <td width="9" height="12" valign="top" align="right"><img src="resources/bar-bottom-left.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
        <td background="resources/bar-border-bottom.gif"><img src="resources/void.gif" width="1" height="12" vspace="0" hspace="0" border="0"/></td>
        <td width="9" height="12" valign="top" align="left"><img src="resources/bar-bottom-right.gif" width="9" height="12" vspace="0" hspace="0" border="0"/></td>
      </tr>
     </table>
     <table border="0" width="85%" cellspacing="0" cellpadding="0">
      <tr>
       <td>
        <font face="arial,helvetica,sanserif" color="#000000"><xsl:apply-templates/></font>
       </td>
      </tr>
    </table>
   </div>
   <br/>
  </xsl:template>
    
<!-- ====================================================================== -->
<!-- footer section -->
<!-- ====================================================================== -->

 <xsl:template match="footer">
  <!-- ignore on general documents -->
 </xsl:template>

<!-- ====================================================================== -->
<!-- paragraph section -->
<!-- ====================================================================== -->

  <xsl:template match="p">
    <p align="justify"><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="note">
   <p>
    <table width="100%" cellspacing="3" cellpadding="0" border="0">
      <tr>
        <td width="28" valign="top">
          <img src="resources/note.gif" width="28" height="29" vspace="0" hspace="0" border="0" alt="Note"/>
        </td>
        <td valign="top">
          <font size="-1" face="arial,helvetica,sanserif" color="#000000">
            <i>
              <xsl:apply-templates/>
            </i>
          </font>
        </td>
      </tr>  
    </table>
   </p>
  </xsl:template>

  <xsl:template match="source">
   <div align="center">
    <table cellspacing="4" cellpadding="0" border="0">
    <tr>
      <td bgcolor="#023264" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#023264" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#023264" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    <tr>
      <td bgcolor="#023264" width="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#ffffff"><pre><xsl:apply-templates/></pre></td>
      <td bgcolor="#023264" width="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    <tr>
      <td bgcolor="#023264" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#023264" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
      <td bgcolor="#023264" width="1" height="1"><img src="resources/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/></td>
    </tr>
    </table>
   </div>
  </xsl:template>
  
  <xsl:template match="fixme">
   <!-- ignore on documentation -->
  </xsl:template>

<!-- ====================================================================== -->
<!-- list section -->
<!-- ====================================================================== -->

 <xsl:template match="ul|ol|dl">
  <blockquote>
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </blockquote>
 </xsl:template>
 
 <xsl:template match="li">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="sl">
  <ul>
   <xsl:apply-templates/>
  </ul>
 </xsl:template>

 <xsl:template match="dt">
  <li>
   <strong><xsl:value-of select="."/></strong>
   <xsl:text> - </xsl:text>
   <xsl:value-of select="following::dd"/>   
  </li>
 </xsl:template>
 
 <xsl:template match="dd">
  <!-- ignore since already used -->
 </xsl:template>

<!-- ====================================================================== -->
<!-- table section -->
<!-- ====================================================================== -->

  <xsl:template match="table">
    <table width="100%" border="0" cellspacing="2" cellpadding="2">
      <caption><xsl:value-of select="caption"/></caption>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="tr">
    <tr><xsl:apply-templates/></tr>
  </xsl:template>

  <xsl:template match="th">
    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
        <b><xsl:apply-templates/></b>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="td">
    <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
      <font color="#000000" size="-1" face="arial,helvetica,sanserif">
        <xsl:apply-templates/>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="tn">
    <td bgcolor="#ffffff" colspan="{@colspan}" rowspan="{@rowspan}">
      &#160;
    </td>
  </xsl:template>
  
  <xsl:template match="caption">
    <!-- ignore since already used -->
  </xsl:template>

<!-- ====================================================================== -->
<!-- markup section -->
<!-- ====================================================================== -->

 <xsl:template match="strong">
   <b><xsl:apply-templates/></b>
 </xsl:template>

 <xsl:template match="em">
    <i><xsl:apply-templates/></i>
 </xsl:template>

 <xsl:template match="filename">
    <filename><i><xsl:apply-templates/></i></filename>
 </xsl:template>

 <xsl:template match="code">
    <code><font face="courier, monospaced"><xsl:apply-templates/></font></code>
 </xsl:template>
 
 <xsl:template match="vtl">
    <vtl><font face="courier, monospaced"><xsl:apply-templates/></font></vtl>
 </xsl:template>

 <xsl:template match="vtldirective">
    <vtldirective><font face="courier, monospaced"><xsl:apply-templates/></font></vtldirective>
 </xsl:template>

 <xsl:template match="variable">
    <variable><font face="courier, monospaced"><xsl:apply-templates/></font></variable>
 </xsl:template>

 <xsl:template match="dummyvariable">
    <dummyvariable><font face="courier, monospaced"><xsl:apply-templates/></font></dummyvariable>
 </xsl:template>

<!-- ====================================================================== -->
<!-- images section -->
<!-- ====================================================================== -->

 <xsl:template match="figure">
  <p align="center"><img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4"/></p>
 </xsl:template>
 
 <xsl:template match="img">
   <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4" align="right"/>
 </xsl:template>

 <xsl:template match="icon">
   <img src="{@src}" alt="{@alt}" border="0" align="absmiddle"/>
 </xsl:template>

<!-- ====================================================================== -->
<!-- links section -->
<!-- ====================================================================== -->

 <xsl:template match="link">
   <a href="{@href}"><xsl:apply-templates/></a>
 </xsl:template>

 <xsl:template match="connect">
  <xsl:apply-templates/>
 </xsl:template>

 <xsl:template match="jump">
   <a href="{@href}#{@anchor}"><xsl:apply-templates/></a>
 </xsl:template>

 <xsl:template match="fork">
   <a href="{@href}" target="_blank"><xsl:apply-templates/></a>
 </xsl:template>

 <xsl:template match="anchor">
   <a name="{@id}"><xsl:comment>anchor</xsl:comment></a>
 </xsl:template>  

<!-- ====================================================================== -->
<!-- specials section -->
<!-- ====================================================================== -->

 <xsl:template match="br">
  <br/>
 </xsl:template>

</xsl:stylesheet>
