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

    <xsl:template match="User">
    	<xsl:if test="./sapUserName">
	    	<xsl:if test="./sapRoleList/role/name">
			    <BAPI_USER_ACTGROUPS_ASSIGN>
			    	<xsl:apply-templates select="./sapUserName" />
			    	<xsl:apply-templates select="./sapRoleList" />
				</BAPI_USER_ACTGROUPS_ASSIGN>
			</xsl:if>	
		</xsl:if>	
    </xsl:template>

    <!-- BEGIN Username parameter -->
    <xsl:template match="sapUserName">
    	<USERNAME><xsl:value-of select="." /></USERNAME>
    </xsl:template>
    <!-- END Username parameter -->
    
    <!-- BEGIN Activitiy Groups Table parameter -->
    <xsl:template match="sapRoleList">
    	<xsl:if test="./role/name">
		   	<ACTIVITYGROUPS>
		   		<xsl:apply-templates select="./role" />	   		
		   	</ACTIVITYGROUPS>	    	
	   	</xsl:if>
    </xsl:template>
    <!-- END Activitiy Groups Table parameter -->
    
    <!-- BEGIN Activitiy Groups Table fields -->
    <xsl:template match="role">
    	<item>
   			<xsl:apply-templates select="./name" />
   			<xsl:apply-templates select="./validFromDate" />
   			<xsl:apply-templates select="./validToDate" />
    	</item>
    </xsl:template>
    
    <xsl:template match="name">
    	<AGR_NAME><xsl:value-of select="." /></AGR_NAME>
    </xsl:template>
    
    <xsl:template match="validFromDate">
    	<FROM_DAT><xsl:value-of select="." /></FROM_DAT>
    </xsl:template>
    
    <xsl:template match="validToDate">
    	<TO_DAT><xsl:value-of select="." /></TO_DAT>
    </xsl:template>
    <!-- END Activitiy Groups Table fields -->
    
</xsl:stylesheet>
