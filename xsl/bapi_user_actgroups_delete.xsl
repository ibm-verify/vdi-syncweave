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
    <xsl:template match="/User">
    	<xsl:if test="./sapUserName">
	    	<xsl:if test="./sapRoleList">
		    	<BAPI_USER_ACTGROUPS_DELETE>
		    		<xsl:apply-templates select="./sapUserName" />
		    	</BAPI_USER_ACTGROUPS_DELETE>
		    </xsl:if>
	    </xsl:if>
    </xsl:template>
    
    <xsl:template match="sapUserName">
    	<USERNAME><xsl:value-of select="." /></USERNAME>
    </xsl:template>
</xsl:stylesheet>
