<?xml version="1.0"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes"/>

<xsl:template match="/">
<xsl:apply-templates select="MetamergeConfig"/>
</xsl:template>

<xsl:template match="MetamergeConfig">
<html>
<title>AL CONFIGURATION REPORT</title>
<body>
<xsl:apply-templates select="Folder"/>
</body>
</html>
</xsl:template>

<xsl:template match="Folder">
<xsl:for-each select="AssemblyLine">
    <h1>AL Name : <xsl:value-of select="@name"/></h1>
	<xsl:apply-templates select="Settings|ContainerEF|ContainerDF|ThreadOptions|CallReturn"/>
</xsl:for-each>
</xsl:template>



<xsl:template match="Settings">
	<h2>Settings</h2>
	<h3>Parameters</h3>
	<table bgcolor="#AEFBFF" border="2">
		<tr align="CENTER" valign="MIDDLE">
			<td width="5" height="3"><b>Name</b></td>
			<td width="5" height="3"><b>Value</b></td>
		</tr>
		<xsl:for-each select="parameter">
		<tr align="CENTER" valign="MIDDLE">
		<td width="5" height="3"><xsl:value-of select="@name"/></td>
		<td width="5" height="3"><xsl:value-of select="."/></td>
		</tr>
		</xsl:for-each>
	</table>
</xsl:template>

<xsl:template match="ContainerEF">
	<h2>Entry Feed Container</h2>
	<h2>Connectors</h2>
		<xsl:for-each select="Connector">
			<h3>Connector Name : <xsl:value-of select="@name"/></h3>
			<xsl:apply-templates select="InheritFrom|ConnectorMode|Enabled|Configuration|Parser"/>
		</xsl:for-each>
</xsl:template>

<xsl:template match="ContainerDF">
	<h2>Data Flow Container</h2>
	<h2>Connectors</h2>
		<xsl:for-each select="Connector">
			<h3>Connector Name : <xsl:value-of select="@name"/></h3>
			<xsl:apply-templates select="InheritFrom|ConnectorMode|Enabled|Configuration|Parser"/>
		</xsl:for-each>
</xsl:template>

<xsl:template match="InheritFrom">
	<br><b>InheritFrom : </b>
	<xsl:value-of select="."/>
	</br>
</xsl:template>

<xsl:template match="ConnectorMode">
	<br><b>ConnectorMode : </b>
	<xsl:value-of select="."/>
	</br>
</xsl:template>

<xsl:template match="Enabled">
	<br><b>Enabled : </b>
	<xsl:value-of select="."/>
	</br>
</xsl:template>


<xsl:template match="Configuration">
	<br>
	<b>Connector parameters</b>
	<table bgcolor="#AEFBFF" border="2">
		<tr align="CENTER" valign="MIDDLE">
			<td width="5" height="3"><b>Name</b></td>
			<td width="5" height="3"><b>Value</b></td>
		</tr>
		<xsl:for-each select="parameter">
		<tr align="CENTER" valign="MIDDLE">
		<td width="5" height="3"><xsl:value-of select="@name"/></td>
		<td width="5" height="3"><xsl:value-of select="."/></td>
		</tr>
		</xsl:for-each>
	</table>
	</br>
</xsl:template>

<xsl:template match="Parser">
	<h3>Parser</h3>
	<xsl:apply-templates select="InheritFrom"/>
	<br><b>Parameters</b></br>
	<table bgcolor="#B7FFD3" border="2">
		<tr align="CENTER" valign="MIDDLE">
			<td width="5" height="3"><b>Name</b></td>
			<td width="5" height="3"><b>Value</b></td>
		</tr>
		<xsl:for-each select="parameter">
		<tr align="CENTER" valign="MIDDLE">
		<td width="5" height="3"><xsl:value-of select="@name"/></td>
		<td width="5" height="3"><xsl:value-of select="."/></td>
		</tr>
		</xsl:for-each>
	</table>
</xsl:template>
</xsl:stylesheet>