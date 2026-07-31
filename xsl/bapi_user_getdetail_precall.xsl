<!--
 Created on 3/09/2004.

 Licensed Materials - Property of IBM
 (c) Copyright International Business Machines Corp. 2004, 2006
 All Rights Reserved.
 restricted by -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
    <xsl:param name="sapUserName" />
    <xsl:template match="/">
    	<BAPI_USER_GET_DETAIL>
    		<USERNAME><xsl:value-of select="$sapUserName" /></USERNAME>
    	</BAPI_USER_GET_DETAIL>
    </xsl:template>
</xsl:stylesheet>
