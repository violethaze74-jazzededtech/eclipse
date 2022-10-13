<?xml version="1.0" encoding="UTF-8"?>
<!--
  This stylesheet upgrades the runtime element to java8
-->
<xsl:stylesheet version="1.0"
  xmlns="http://appengine.google.com/ns/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:appengine="http://appengine.google.com/ns/1.0"
  exclude-result-prefixes="appengine">

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

  <xsl:template match="appengine:appengine-web-app"><appengine-web-app>
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
     </xsl:copy>
  <runtime>java8</runtime>
</appengine-web-app></xsl:template>

</xsl:stylesheet>