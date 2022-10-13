<?xml version="1.0" encoding="UTF-8"?>
<!--
  This stylesheet updates the servlet version to 3.1
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://xmlns.jcp.org/xml/ns/javaee/">

  <xsl:template match="/">
  <xsl:text>
</xsl:text>
    <xsl:apply-templates select="node()"/>
  </xsl:template>

  <xsl:template match="web-app">
    <web-app
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
        version="3.1">
      <xsl:apply-templates select="node()"/>
    </web-app>
  </xsl:template>
  
  <xsl:template match="*" >
    <xsl:element name="{local-name()}" namespace="http://xmlns.jcp.org/xml/ns/javaee/">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
