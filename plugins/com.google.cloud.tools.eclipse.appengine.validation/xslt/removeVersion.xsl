<?xml version="1.0" encoding="UTF-8"?>
<!--
	This stylesheet removes a <version/> element from appengine-web.xml
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:appengine="http://appengine.google.com/ns/1.0">

  <xsl:template match="/">
  <xsl:text>
</xsl:text>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="appengine:version"/>

</xsl:stylesheet>