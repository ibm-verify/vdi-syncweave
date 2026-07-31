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

    <xsl:template match="/sapPersonalData">
    	<xsl:choose>
	    	<xsl:when test="string-length($EmployeeNumber)>0">
		    	<BAPI_EMPLOYEE_ENQUEUE>
		    		<NUMBER><xsl:value-of select="$EmployeeNumber" /></NUMBER>
		    	</BAPI_EMPLOYEE_ENQUEUE>
		    </xsl:when>
		    <xsl:otherwise>
		    	<xsl:if test="./sapBorObjIdentifier/EmployeeNumber">
			    	<BAPI_EMPLOYEE_ENQUEUE>
			    		<NUMBER><xsl:value-of select="./sapBorObjIdentifier/EmployeeNumber" /></NUMBER>
			    	</BAPI_EMPLOYEE_ENQUEUE>
		    	</xsl:if>
		    </xsl:otherwise>
    	</xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
