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
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:doc="urn:sap-com:document:sap:business:rfc"
    exclude-result-prefixes="doc">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
    <xsl:template match="doc:BAPI_PERSDATA_GETDETAILEDLIST.Response">
    	<xsl:if test="./PERSONALDATA/item">
	    	<sapPersonalData>
	    		<xsl:apply-templates select="./EMPLOYEENUMBER" />
	    		<xsl:apply-templates select="./PERSONALDATA" />
	    	</sapPersonalData>
	    </xsl:if>
    </xsl:template>
    
    <xsl:template match="EMPLOYEENUMBER">
    	<EmployeeNumber><xsl:value-of select="." /></EmployeeNumber>
    </xsl:template>
    
    <xsl:template match="PERSONALDATA">
    	<xsl:if test="./item">
	    	<personalDataDetailList>
		    	<xsl:apply-templates select="./item" />    		
	    	</personalDataDetailList>
    	</xsl:if>
	
    </xsl:template>
    <xsl:template match="PERSONALDATA/item">
   		<personalDataDetail>
    		<xsl:apply-templates select="./SUBTYPE" />
    		<xsl:apply-templates select="./OBJECTID" />
    		<xsl:apply-templates select="./LOCKINDIC" />
    		<xsl:apply-templates select="./VALIDEND" />
    		<xsl:apply-templates select="./VALIDBEGIN" />
    		<xsl:apply-templates select="./RECORDNR" />
    		<xsl:apply-templates select="./FORMOFADDRESS" />
    		<xsl:apply-templates select="./FIRSTNAME" />
    		<xsl:apply-templates select="./LASTNAME" />
    		<xsl:apply-templates select="./NAMEATBIRTH" />
    		<xsl:apply-templates select="./KNOWNAS" />
    		<xsl:apply-templates select="./ACADEMICGRADE" />
    		<xsl:apply-templates select="./ARISTROCRATICTITLE" />
    		<xsl:apply-templates select="./SURNAMEPREFIX" />    		
    		<xsl:apply-templates select="./GENDER" />    		
    		<xsl:apply-templates select="./DATEOFBIRTH" />    		
    		<xsl:apply-templates select="./BIRTHPLACE" />    		
    		<xsl:apply-templates select="./STATEOFBIRTH" />    		
    		<xsl:apply-templates select="./COUNTRYOFBIRTH" />    		
    		<xsl:apply-templates select="./MARITALSTATUS" />    		
    		<xsl:apply-templates select="./NUMBEROFCHILDREN" />    		
    		<xsl:apply-templates select="./RELIGION" />    		
    		<xsl:apply-templates select="./LANGUAGE" />    		
    		<xsl:apply-templates select="./LANGUAGE_ISO" />    		
    		<xsl:apply-templates select="./NATIONALITY" />    		
    		<xsl:apply-templates select="./IDNUMBER" />    		
    	</personalDataDetail>
   	</xsl:template>
    <xsl:template match="SUBTYPE">
   		<SubType><xsl:value-of select="." /></SubType>
    </xsl:template>
    <xsl:template match="OBJECTID">
   		<ObjectID><xsl:value-of select="." /></ObjectID>
    </xsl:template>
    <xsl:template match="LOCKINDIC">
   		<LockIndicator><xsl:value-of select="." /></LockIndicator>
    </xsl:template>
    <xsl:template match="VALIDEND">
   		<ValidityEnd><xsl:value-of select="." /></ValidityEnd>
    </xsl:template>
    <xsl:template match="VALIDBEGIN">
   		<ValidityBegin><xsl:value-of select="." /></ValidityBegin>
    </xsl:template>
    <xsl:template match="RECORDNR">
   		<RecordNumber><xsl:value-of select="." /></RecordNumber>
    </xsl:template>
    <xsl:template match="FORMOFADDRESS">
   		<title><xsl:value-of select="." /></title>
    </xsl:template>
    <xsl:template match="FIRSTNAME">
   		<firstname><xsl:value-of select="." /></firstname>
    </xsl:template>
    <xsl:template match="LASTNAME">
   		<lastname><xsl:value-of select="." /></lastname>
    </xsl:template>
    <xsl:template match="NAMEATBIRTH">
   		<nameAtBirth><xsl:value-of select="." /></nameAtBirth>
    </xsl:template>
    <xsl:template match="KNOWNAS">
   		<knownAs><xsl:value-of select="." /></knownAs>
    </xsl:template>
    <xsl:template match="ACADEMICGRADE">
   		<academicGrade><xsl:value-of select="." /></academicGrade>
    </xsl:template>
    <xsl:template match="ARISTROCRATICTITLE">
   		<aristocraticTitle><xsl:value-of select="." /></aristocraticTitle>
    </xsl:template>
    <xsl:template match="SURNAMEPREFIX">
   		<surnamePrefix><xsl:value-of select="." /></surnamePrefix>
    </xsl:template>
    <xsl:template match="GENDER">
   		<gender><xsl:value-of select="." /></gender>
    </xsl:template>
    <xsl:template match="DATEOFBIRTH">
   		<dateOfBirth><xsl:value-of select="." /></dateOfBirth>
    </xsl:template>
    <xsl:template match="BIRTHPLACE">
   		<birthPlace><xsl:value-of select="." /></birthPlace>
    </xsl:template>
    <xsl:template match="STATEOFBIRTH">
   		<stateOfBirth><xsl:value-of select="." /></stateOfBirth>
    </xsl:template>
    <xsl:template match="COUNTRYOFBIRTH">
   		<countryOfBirth><xsl:value-of select="." /></countryOfBirth>
    </xsl:template>
    <xsl:template match="MARITALSTATUS">
   		<maritalStatus><xsl:value-of select="." /></maritalStatus>
    </xsl:template>
    <xsl:template match="NUMBEROFCHILDREN">
   		<numberOfChildren><xsl:value-of select="." /></numberOfChildren>
    </xsl:template>
    <xsl:template match="RELIGION">
   		<religion><xsl:value-of select="." /></religion>
    </xsl:template>
    <xsl:template match="LANGUAGE">
   		<language><xsl:value-of select="." /></language>
    </xsl:template>
    <xsl:template match="LANGUAGE_ISO">
   		<languageCode><xsl:value-of select="." /></languageCode>
    </xsl:template>
    <xsl:template match="NATIONALITY">
   		<nationality><xsl:value-of select="." /></nationality>
    </xsl:template>
    <xsl:template match="IDNUMBER">
   		<idNumber><xsl:value-of select="." /></idNumber>
    </xsl:template>
    
</xsl:stylesheet>
