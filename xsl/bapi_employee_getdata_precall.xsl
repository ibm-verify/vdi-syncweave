<!--
 Created on 3/09/2004.

 Licensed Materials - Property of IBM
 (c) Copyright International Business Machines Corp. 2004, 2006
 All Rights Reserved.
 restricted by -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt">
    
    <xsl:output method="xml" indent="yes" />
    <xsl:template match="/">
	    <BAPI_EMPLOYEE_GETDATA>
	    	<LASTNAME_M>*</LASTNAME_M>
	    </BAPI_EMPLOYEE_GETDATA>
    </xsl:template>
</xsl:stylesheet>
