<?xml version="1.0"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes"/>

	<xsl:param name="tdiLocale">en</xsl:param>
	<xsl:param name="tdiObjectName" />
	<xsl:param name="tdiObjectType" />
	<xsl:param name="tdiConfig" />
	<xsl:param name="tdiHome" />
	
<xsl:template match="/">
	<html>
	<title><xsl:value-of select="$tdiObjectName"/></title>
	<body>
	<h1><xsl:value-of select="$tdiObjectName"/></h1>
	<hr/>
	<xsl:for-each select="//AssemblyLine[@name=$tdiObjectName]">
		<xsl:apply-templates select="." />
	</xsl:for-each>
	</body>
	</html>
</xsl:template>

<xsl:template match="AssemblyLine">
	<xsl:apply-templates select=".//UserComment" />
</xsl:template>

<xsl:template match="UserComment">
	<xsl:if test=". != ''">
		<br><b><xsl:value-of select="../@name"/>: </b><br/>
		<xsl:value-of select="." disable-output-escaping="no" />
		</br>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>