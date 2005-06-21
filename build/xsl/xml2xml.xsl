<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match ="/">
  <documentation>
    <xsl:copy-of select="document('../../xdocs/index.xml')"/>
    <xsl:copy-of select="document('../../xdocs/getting-started.xml')"/>
    <xsl:copy-of select="document('../../xdocs/install.xml')"/>
    <xsl:copy-of select="document('../../xdocs/design.xml')"/>
    <xsl:copy-of select="document('../../xdocs/contributors.xml')"/>
    <xsl:copy-of select="document('../../xdocs/code-standards.xml')"/>
    <xsl:copy-of select="document('../../xdocs/license.xml')"/>
    <xsl:copy-of select="document('../../xdocs/todo.xml')"/>
    <xsl:copy-of select="document('../../xdocs/user-guide.xml')"/>
    <xsl:copy-of select="document('../../xdocs/developer-guide.xml')"/>
  </documentation>
</xsl:template>

</xsl:stylesheet>






