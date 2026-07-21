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
    
    <xsl:output method="xml" indent="yes" />
    <xsl:template match="/">
	    <BAPI_HELPVALUES_GET>
	    	<OBJTYPE>USER</OBJTYPE>
	    	<OBJNAME>USER</OBJNAME>
	    	<METHOD>GETDETAIL</METHOD>
	    	<PARAMETER>USERNAME</PARAMETER>
	    </BAPI_HELPVALUES_GET>
    </xsl:template>
</xsl:stylesheet>
