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
				    <BAPI_PERSDATA_CHANGE>
				    	<xsl:call-template name="sapBorObjIdentifier" />
				    	<xsl:apply-templates select="./personalDataDetail" />
					</BAPI_PERSDATA_CHANGE>
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
