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
    <xsl:template match="/">
    	<xsl:if test="string-length($EmployeeNumber)>0">
	    	<BAPI_PERSDATA_GETDETAILEDLIST>
	    		<EMPLOYEENUMBER><xsl:value-of select="$EmployeeNumber" /></EMPLOYEENUMBER>
	    	</BAPI_PERSDATA_GETDETAILEDLIST>
	    </xsl:if>
    </xsl:template>
</xsl:stylesheet>
