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
    xmlns:xalan="http://xml.apache.org/xslt">
    
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
    
    <xsl:template match="User">
    	<xsl:if test="./sapUserName">
	    	<xsl:if test="./sapProfileList/profile/name">
			    <BAPI_USER_PROFILES_ASSIGN>
			    	<xsl:apply-templates select="./sapUserName" />
			    	<xsl:apply-templates select="./sapProfileList" />
				</BAPI_USER_PROFILES_ASSIGN>
			</xsl:if>	
		</xsl:if>	
    </xsl:template>
    
    <!-- BEGIN Username parameter -->
    <xsl:template match="sapUserName">
    	<USERNAME><xsl:value-of select="." /></USERNAME>
    </xsl:template>
    <!-- END Username parameter -->
    
    <!-- BEGIN Profile Table parameter -->
    <xsl:template match="sapProfileList">
    	<xsl:if test="./profile/name">
		   	<PROFILES>
		   		<xsl:apply-templates select="./profile" />	   		
		   	</PROFILES>	    	
	   	</xsl:if>
    </xsl:template>
    
    <xsl:template match="profile">
    	<item>
	    	<BAPIPROF><xsl:value-of select="." /></BAPIPROF>
    	</item>
    </xsl:template>
</xsl:stylesheet>
