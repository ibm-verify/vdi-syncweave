<!--
 Created on 3/09/2004.

 Licensed Materials - Property of IBM
 (c) Copyright International Business Machines Corp. 2004, 2006
 All Rights Reserved.
 US Government Users Restricted Rights - Use, duplicaion or disclosure
 restricted by GSA ADP Schedule Contract with IBM Corp.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:doc="urn:sap-com:document:sap:business:rfc"
    exclude-result-prefixes="doc">

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
    <xsl:template match="/">
    	<UserList>
    		<xsl:apply-templates select="//VALUES_FOR_FIELD" />
    	</UserList>
    </xsl:template>

    <xsl:template match="VALUES_FOR_FIELD">
    	<xsl:apply-templates select="./item" />
    </xsl:template>
    <xsl:template match="item">
        <User><xsl:apply-templates select="./VALUES" /></User>
    </xsl:template>

    <xsl:template match="VALUES">
    	<sapUserName><xsl:value-of select="." /></sapUserName>
    </xsl:template>



</xsl:stylesheet>
