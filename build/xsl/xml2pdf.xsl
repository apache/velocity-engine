<?xml version="1.0"?>

<!--    XSLT stylesheet to convert the Fop documentation collected in one xml file into a fo file
        for use in FOP 

TBD: - The faq doesn't show in the content
     - check why margin-bottom on the page with properties is too large
     - check why keep-next not only doesn't work, but leads to repeating already printed lines
     - make lines containing only code look nicer (smaller line height)
     - replace bullets in ordered lists with numbers
     - correct the hack replacing nbsp with '-'
     - handle the links correctly which have been external in the html doc and are now internal

-->

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
     xmlns:fo="http://www.w3.org/1999/XSL/Format"
>

<xsl:template match ="/">
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

		<!-- defines page layout -->
		<fo:layout-master-set>

			<fo:simple-page-master master-name="first"
										page-height="29.7cm" 
										page-width="21cm"
										margin-top="1.5cm" 
										margin-bottom="2cm" 
										margin-left="2.5cm" 
										margin-right="2.5cm">
				<fo:region-body margin-top="3cm"/>
				<fo:region-before extent="1.5cm"/>
				<fo:region-after extent="1.5cm"/>
			</fo:simple-page-master>

			<fo:simple-page-master master-name="rest"
										page-height="29.7cm" 
										page-width="21cm"
										margin-top="1.5cm" 
										margin-bottom="2cm" 
										margin-left="2.5cm" 
										margin-right="2.5cm">
				<fo:region-body margin-top="2.5cm"/>
				<fo:region-before extent="1.5cm"/>
				<fo:region-after extent="1.5cm"/>
			</fo:simple-page-master>

			<fo:page-sequence-master master-name="all">
				<fo:single-page-master-reference master-name="first"/>
				<fo:repeatable-page-master-reference master-name="rest"/>
			</fo:page-sequence-master>

		</fo:layout-master-set>

		<fo:page-sequence master-name="all">
			<fo:static-content flow-name="xsl-region-before">
				<fo:block text-align="end" 
							font-size="10pt" 
							font-family="serif" 
							line-height="14pt" >
					FOP documentation - p. <fo:page-number/>
				</fo:block>
			</fo:static-content> 

			<fo:flow flow-name="xsl-region-body">


       <fo:block font-size="18pt" 
                font-family="sans-serif" 
                line-height="24pt"
                space-after.optimum="15pt"
                background-color="blue"
                color="white"
                text-align="center">
        FOP - an xsl:fo renderer
         </fo:block>


        <!-- generates table of contents and puts it into a table -->

         <fo:block font-size="14pt" 
                  font-family="sans-serif" 
                  line-height="18pt"
                  space-after.optimum="10pt"
                  font-weight="bold"
                  start-indent="15pt">
            Content
         </fo:block>

         <fo:table>
            <fo:table-column column-width="1cm"/>
            <fo:table-column column-width="15cm"/>
            <fo:table-body font-size="12pt" 
                           line-height="16pt"
                           font-family="sans-serif">
              <xsl:for-each select="//s1"> <!-- An dieser Stelle muesste noch ein "oder finde faqs" stehen -->
                <fo:table-row>
                  <fo:table-cell>
                     <fo:block text-align="end" >
                          <xsl:number value="position()" format="1"/>) 
                     </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                     <fo:block  text-align="start" >
                        <fo:simple-link color="blue">
                           <xsl:attribute name="internal-destination">
                           <xsl:value-of select="translate(.,' ),-.(','____')"/>
                           </xsl:attribute>
                          <xsl:value-of select="@title"/>
                        </fo:simple-link> 
                     </fo:block>
                  </fo:table-cell>
               </fo:table-row>
            </xsl:for-each>
            </fo:table-body>
         </fo:table>


			<xsl:apply-templates select="documentation"/> 
			</fo:flow>
		</fo:page-sequence>
	</fo:root>
</xsl:template>

<!-- s1 -->
<xsl:template match ="s1">
   <fo:block font-size="18pt" 
            font-family="sans-serif" 
            line-height="24pt"
            space-before.optimum="15pt"
            space-after.optimum="15pt"
            background-color="blue"
            color="white"
            text-align="center"
            >
     <xsl:attribute name="id">
     <xsl:value-of select="translate(.,' ),-.(','____')"/>
     </xsl:attribute>
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- s2 -->
<xsl:template match ="s2">
   <fo:block font-size="16pt" 
            font-family="sans-serif" 
            line-height="20pt"
            space-before.optimum="15pt"
            space-after.optimum="12pt"
            text-align="center"
            padding-top="3pt"
            >
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- s3 -->
<xsl:template match ="s3">
   <fo:block font-size="14pt" 
            font-family="sans-serif" 
            line-height="18pt"
            space-before.optimum="10pt"
            space-after.optimum="9pt"
            text-align="center"
            padding-top="3pt">
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>

<!-- p  [not(code)] -->
<xsl:template match ="p"> 
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="13pt"
            space-after.optimum="3pt"
            space-before.optimum="3pt"
            text-align="justify">
     <xsl:apply-templates/> 
   </fo:block>
</xsl:template>

<!-- p + code 
<xsl:template match ="p[code]">
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="11pt"
            space-after.optimum="0pt"
            space-before.optimum="0pt"
            text-align="start">
     <xsl:apply-templates/> 
   </fo:block>
</xsl:template>
-->

<!-- faqs -->
<xsl:template match ="faqs">
   <fo:block font-size="18pt" 
            font-family="sans-serif" 
            line-height="24pt"
            space-before.optimum="15pt"
            space-after.optimum="15pt"
            background-color="blue"
            color="white"
            text-align="center"
            >
     <xsl:attribute name="id">
     <xsl:value-of select="translate(.,' ),-.(','____')"/>
     </xsl:attribute>
     <xsl:value-of select="@title"/>
   </fo:block>
    <xsl:apply-templates/> 
</xsl:template>


<!-- faq -->
<xsl:template match ="faq">
    <xsl:apply-templates/> 
</xsl:template>

<!-- q in faq -->
<xsl:template match ="q">
   <fo:block font-size="11pt" 
            font-family="sans-serif" 
            line-height="13pt"
            space-after.optimum="3pt"
            space-before.optimum="3pt"
            text-align="justify">
      <xsl:apply-templates/> 
    </fo:block>
</xsl:template>

<!-- a in faq -->
<xsl:template match ="a">
      <xsl:apply-templates/> 
</xsl:template>


<!-- jump (links) -->
<xsl:template match ="*/jump">
   <fo:simple-link color="blue" external-destination="{@href}">
     <xsl:apply-templates/> 
   </fo:simple-link>
</xsl:template>


<!-- code -->
<xsl:template match ="*/code">
   <fo:inline font-size="10pt" 
            font-family="Courier">
     <xsl:apply-templates/> 
   </fo:inline>
</xsl:template>


<!-- ul (unordered list) -->
<xsl:template match ="ul">
  <fo:list-block start-indent="1cm" 
                 provisional-distance-between-starts="12pt" 
                 font-family="sans-serif" 
                 font-size="11pt" 
                 line-height="11pt">
     <xsl:apply-templates/> 
   </fo:list-block>
</xsl:template>


<!-- ol (ordered list) -->
<xsl:template match ="ol">
  <fo:list-block start-indent="1cm" 
                 provisional-distance-between-starts="12pt" 
                 font-family="sans-serif" 
                 font-size="11pt" 
                 line-height="11pt">
     <xsl:apply-templates/> 
   </fo:list-block>
</xsl:template>


<!-- li (list item) in unordered list -->
<xsl:template match ="ul/li">
    <fo:list-item>
      <fo:list-item-label>
        <fo:block><fo:inline font-family="Symbol">&#183;</fo:inline></fo:block>
      </fo:list-item-label>
      <fo:list-item-body>
        <fo:block space-after.optimum="4pt"
              text-align="justify"
              padding-top="3pt">
          <xsl:apply-templates/> 
       </fo:block>
      </fo:list-item-body>
    </fo:list-item>
</xsl:template>


<!-- li (list item) in ordered list -->
<xsl:template match ="ol/li">
    <fo:list-item>
      <fo:list-item-label>
        <fo:block>
          <xsl:number level="multiple" count="li" format="1"/>)
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body>
        <fo:block space-after.optimum="4pt"
              text-align="justify"
              padding-top="3pt">
          <xsl:apply-templates/> 
       </fo:block>
      </fo:list-item-body>
    </fo:list-item>
</xsl:template>

<!-- end body -->

</xsl:stylesheet>
