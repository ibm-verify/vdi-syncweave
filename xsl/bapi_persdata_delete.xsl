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
    <xsl:param name="EmployeeNumber" />
    <xsl:param name="ValidityBegin" />
    <xsl:param name="ValidityEnd" />
    <xsl:param name="Subtype" />
    <xsl:param name="LockIndicator" />
    <xsl:param name="ObjectID" />
    <xsl:param name="RecordNumber" />
    
    <xsl:template match="sapPersonalData">
    	<xsl:if test="$EmployeeNumber">
 		   	<xsl:if test="$ValidityBegin">
 			   	<xsl:if test="$ValidityEnd">
				    <BAPI_PERSDATA_DELETE>
				    	<xsl:call-template name="sapBorObjIdentifier" />
					</BAPI_PERSDATA_DELETE>
				</xsl:if>
			</xsl:if>
		</xsl:if>
    </xsl:template>
    
    <xsl:template name="sapBorObjIdentifier">
    	<EMPLOYEENUMBER><xsl:value-of select="$EmployeeNumber" /></EMPLOYEENUMBER>
    	<VALIDITYBEGIN><xsl:value-of select="$ValidityBegin" /></VALIDITYBEGIN>
    	<VALIDITYEND><xsl:value-of select="$ValidityEnd" /></VALIDITYEND>
		<xsl:choose>
    		<xsl:when test="$Subtype">
    			<SUBTYPE><xsl:value-of select="$Subtype" /></SUBTYPE>
    		</xsl:when>
    		<xsl:otherwise>
    			<SUBTYPE>    </SUBTYPE>
    		</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
    		<xsl:when test="$LockIndicator">
    			<LOCKINDICATOR><xsl:value-of select="$LockIndicator" /></LOCKINDICATOR>
    		</xsl:when>
    		<xsl:otherwise>
    			<LOCKINDICATOR> </LOCKINDICATOR>
    		</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
    		<xsl:when test="$ObjectID">
    			<OBJECTID><xsl:value-of select="$ObjectID" /></OBJECTID>
    		</xsl:when>
    		<xsl:otherwise>
    			<OBJECTID>  </OBJECTID>
    		</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
    		<xsl:when test="$RecordNumber">
    			<RECORDNUMBER><xsl:value-of select="$RecordNumber" /></RECORDNUMBER>
    		</xsl:when>
    		<xsl:otherwise>
    			<RECORDNUMBER>000</RECORDNUMBER>
    		</xsl:otherwise>
		</xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
