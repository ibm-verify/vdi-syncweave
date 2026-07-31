<!--
 Created on 3/09/2004.

 Licensed Materials - Property of IBM
 (c) Copyright International Business Machines Corp. 2004, 2006
 All Rights Reserved.
 restricted by -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:doc="urn:sap-com:document:sap:business:rfc"
    exclude-result-prefixes="doc">

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
    <xsl:template match="/">
    	<sapBorObjIdentifierList>
    		<xsl:apply-templates select="//PERSONAL_DATA" />
    	</sapBorObjIdentifierList>
    </xsl:template>

    <xsl:template match="PERSONAL_DATA">
    	<xsl:apply-templates select="./item" />
    </xsl:template>
    <xsl:template match="item">
        <sapBorObjIdentifier>
        	<xsl:apply-templates select="./PERNO" />
        	<xsl:apply-templates select="./SUBTYPE" />
        	<xsl:apply-templates select="./LOCK_IND" />
        	<xsl:apply-templates select="./FROM_DATE" />
        	<xsl:apply-templates select="./TO_DATE" />
        	<xsl:apply-templates select="./OBJECT_ID" />
        	<xsl:apply-templates select="./SEQNO" />
        </sapBorObjIdentifier>
    </xsl:template>

    <xsl:template match="PERNO">
    	<EmployeeNumber><xsl:value-of select="." /></EmployeeNumber>
    </xsl:template>
    <xsl:template match="SUBTYPE">
    	<SubType><xsl:value-of select="." /></SubType>
    </xsl:template>
    <xsl:template match="LOCK_IND">
    	<LockIndicator><xsl:value-of select="." /></LockIndicator>
    </xsl:template>
    <xsl:template match="FROM_DATE">
    	<ValidityBegin><xsl:value-of select="." /></ValidityBegin>
    </xsl:template>
    <xsl:template match="TO_DATE">
    	<ValidityEnd><xsl:value-of select="." /></ValidityEnd>
    </xsl:template>
    <xsl:template match="OBJECT_ID">
    	<ObjectID><xsl:value-of select="." /></ObjectID>
    </xsl:template>
    <xsl:template match="SEQNO">
    	<RecordNumber><xsl:value-of select="." /></RecordNumber>
    </xsl:template>



</xsl:stylesheet>
