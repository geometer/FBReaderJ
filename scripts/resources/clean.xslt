<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="comment()"/>
  <xsl:template match="node">
		<xsl:if test="@name != ''">
      <node name="{@name}">
        <xsl:apply-templates/>
      </node>
		</xsl:if>
  </xsl:template>
  <xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
  </xsl:template>
</xsl:stylesheet>
