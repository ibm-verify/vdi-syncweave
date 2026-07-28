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
    
    <xsl:template match="sapPersonalData">
    	<xsl:if test="./sapBorObjIdentifier/EmployeeNumber">
 		   	<xsl:if test="./sapBorObjIdentifier/ValidityBegin">
 			   	<xsl:if test="./sapBorObjIdentifier/ValidityEnd">
				    <BAPI_PERSDATA_CREATE>
				    	<xsl:apply-templates select="./sapBorObjIdentifier" />
				    	<xsl:apply-templates select="./personalDataDetail" />
					</BAPI_PERSDATA_CREATE>
				</xsl:if>
			</xsl:if>
		</xsl:if>
    </xsl:template>
    
    <xsl:template match="sapBorObjIdentifier">
    	<xsl:apply-templates select="./EmployeeNumber" />
    	<xsl:apply-templates select="./ValidityBegin" />
    	<xsl:apply-templates select="./ValidityEnd" />
    </xsl:template>
    <xsl:template match="EmployeeNumber">
    	<EMPLOYEENUMBER><xsl:value-of select="." /></EMPLOYEENUMBER>
    </xsl:template>
    <xsl:template match="ValidityBegin">
    	<VALIDITYBEGIN><xsl:value-of select="." /></VALIDITYBEGIN>
    </xsl:template>
    <xsl:template match="ValidityEnd">
    	<VALIDITYEND><xsl:value-of select="." /></VALIDITYEND>
    </xsl:template>
    
    <xsl:template match="personalDataDetail">
		<xsl:apply-templates select="./title" />	    	
		<xsl:apply-templates select="./firstname" />	    	
		<xsl:apply-templates select="./lastname" />	    	
		<xsl:apply-templates select="./nameAtBirth" />	    	
		<xsl:apply-templates select="./knownAs" />	    	
		<xsl:apply-templates select="./academicGrade" />	    	
	    <xsl:apply-templates select="./aristocraticTitle" />
	    <xsl:apply-templates select="./surnamePrefix" />
	    <xsl:apply-templates select="./gender" />
	    <xsl:apply-templates select="./dateOfBirth" />
	    <xsl:apply-templates select="./birthPlace" />
	    <xsl:apply-templates select="./stateOfBirth" />
	    <xsl:apply-templates select="./countryOfBirth" />
	    <xsl:apply-templates select="./maritalStatus" />
	    <xsl:apply-templates select="./numberOfChildren" />
	    <xsl:apply-templates select="./religion" />
	    <xsl:apply-templates select="./language" />
	    <xsl:apply-templates select="./languageCode" />
	    <xsl:apply-templates select="./nationality" />
	    <xsl:apply-templates select="./idNumber" />
    </xsl:template>
    
    <xsl:template match="title">
    	<FORMOFADDRESS><xsl:value-of select="." /></FORMOFADDRESS>
    </xsl:template>
    <xsl:template match="firstname">
    	<FIRSTNAME><xsl:value-of select="." /></FIRSTNAME>
    </xsl:template>
    <xsl:template match="lastname">
    	<LASTNAME><xsl:value-of select="." /></LASTNAME>
    </xsl:template>
    <xsl:template match="nameAtBirth">
    	<NAMEATBIRTH><xsl:value-of select="." /></NAMEATBIRTH>
    </xsl:template>
    <xsl:template match="knownAs">
    	<KNOWNAS><xsl:value-of select="." /></KNOWNAS>
    </xsl:template>
    <xsl:template match="academicGrade">
    	<ACADEMICGRADE><xsl:value-of select="." /></ACADEMICGRADE>	
    </xsl:template>
    <xsl:template match="aristocraticTitle">
    	<ARISTROCRATICTITLE><xsl:value-of select="." /></ARISTROCRATICTITLE>	
    </xsl:template>
    <xsl:template match="surnamePrefix">
    	<SURNAMEPREFIX><xsl:value-of select="." /></SURNAMEPREFIX>
    </xsl:template>
    <xsl:template match="gender">
    	<GENDER><xsl:value-of select="." /></GENDER>
    </xsl:template>
    <xsl:template match="dateOfBirth">
    	<DATEOFBIRTH><xsl:value-of select="." /></DATEOFBIRTH>
    </xsl:template>
    <xsl:template match="birthPlace">
    	<BIRTHPLACE><xsl:value-of select="." /></BIRTHPLACE>
    </xsl:template>
    <xsl:template match="stateOfBirth">
    	<STATEOFBIRTH><xsl:value-of select="." /></STATEOFBIRTH>
    </xsl:template>
    <xsl:template match="countryOfBirth">
    	<COUNTRYOFBIRTH><xsl:value-of select="." /></COUNTRYOFBIRTH>
    </xsl:template>
    <xsl:template match="maritalStatus">
    	<MARITALSTATUS><xsl:value-of select="." /></MARITALSTATUS>
    </xsl:template>
    <xsl:template match="numberOfChildren">
    	<NUMBEROFCHILDREN><xsl:value-of select="." /></NUMBEROFCHILDREN>
    </xsl:template>
    <xsl:template match="religion">
    	<RELIGION><xsl:value-of select="." /></RELIGION>
    </xsl:template>
    <xsl:template match="language">
    	<LANGUAGE><xsl:value-of select="." /></LANGUAGE>
    </xsl:template>
    <xsl:template match="languageCode">
    	<LANGUAGE_ISO><xsl:value-of select="." /></LANGUAGE_ISO>
    </xsl:template>
    <xsl:template match="nationality">
    	<NATIONALITY><xsl:value-of select="." /></NATIONALITY>
    </xsl:template>
    <xsl:template match="idNumber">
    	<IDNUMBER><xsl:value-of select="." /></IDNUMBER>
    </xsl:template>

</xsl:stylesheet>
