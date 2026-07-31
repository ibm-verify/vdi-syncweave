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
    <xsl:template match="doc:BAPI_USER_GET_DETAIL.Response">
    	<User>
    		<xsl:apply-templates select="./USERNAME" />
    		<xsl:apply-templates select="./ALIAS" />
    		<xsl:apply-templates select="./ADDRESS" />
    		<xsl:apply-templates select="./COMPANY" />
    		<xsl:apply-templates select="./DEFAULTS" />
    		<xsl:apply-templates select="./LOGONDATA" />    		
    		<xsl:apply-templates select="./SNC" />    		
    		<xsl:apply-templates select="./GROUPS" />    		
    		<xsl:apply-templates select="./PARAMETER" />    		
    		<xsl:apply-templates select="./ADDSMTP" />    		
	    	<xsl:apply-templates select="./ACTIVITYGROUPS" />
	    	<xsl:apply-templates select="./PROFILES" />
    	</User>    	
    </xsl:template>
    
    <!-- BEGIN sapUserName -->
    <xsl:template match="USERNAME" >
    	<sapUserName><xsl:value-of select="." /></sapUserName>
    </xsl:template>
	<!-- END sapUserName -->    
    
    <!-- BEGIN sapUserAlias -->
    <xsl:template match="ALIAS" >
   		<xsl:apply-templates select="./USERALIAS" />
    </xsl:template>
    <xsl:template match="USERALIAS">
    	<sapUserAlias>
    		<aliasName><xsl:value-of select="." /></aliasName>
    	</sapUserAlias>
    </xsl:template>
	<!-- END sapUserAlias -->    
    
    <!-- BEGIN sapAddress -->
    <xsl:template match="ADDRESS" >
    	<sapAddress>
	    	<xsl:apply-templates select="./TITLE_P" />
	    	<xsl:apply-templates select="./TITLE_ACA1" />
	    	<xsl:apply-templates select="./FIRSTNAME" />
	    	<xsl:apply-templates select="./LASTNAME" />
	    	<xsl:apply-templates select="./PREFIX1" />
	    	<xsl:apply-templates select="./NAMEFORMAT" />
	    	<xsl:apply-templates select="./NAMCOUNTRY" />
	    	<xsl:apply-templates select="./LANGU_ISO" />
			<xsl:if test="./LANGU">
	    		<xsl:call-template name="ADDRESS_LANGU">
	    			<xsl:with-param name="value" select="./LANGU" />
	    		</xsl:call-template>
	    	</xsl:if>
	    	<xsl:apply-templates select="./SORT1_P" />
	    	<xsl:apply-templates select="./DEPARTMENT" />
	    	<xsl:apply-templates select="./FUNCTION" />
	    	<xsl:apply-templates select="./BUILDING_P" />
	    	<xsl:apply-templates select="./FLOOR_P" />
	    	<xsl:apply-templates select="./ROOM_NO_P" />
	    	<xsl:apply-templates select="./NAME" />
	    	<xsl:apply-templates select="./NAME_2" />
	    	<xsl:apply-templates select="./NAME_3" />
	    	<xsl:apply-templates select="./NAME_4" />
	    	<xsl:apply-templates select="./CITY" />
	    	<xsl:apply-templates select="./POSTL_COD1" />
	    	<xsl:apply-templates select="./POSTL_COD2" />
	    	<xsl:apply-templates select="./PO_BOX" />
	    	<xsl:apply-templates select="./STREET" />
	    	<xsl:apply-templates select="./STREET_NO" />
	    	<xsl:apply-templates select="./HOUSE_NO" />
	    	<xsl:apply-templates select="./COUNTRY" />
	    	<xsl:apply-templates select="./COUNTRYISO" />
	    	<xsl:apply-templates select="./REGION" />
	    	<xsl:apply-templates select="./TIME_ZONE" />
	    	<xsl:apply-templates select="./TEL1_NUMBR" />
	    	<xsl:apply-templates select="./TEL1_EXT" />
	    	<xsl:apply-templates select="./FAX_NUMBER" />
	    	<xsl:apply-templates select="./FAX_EXTENS" />
	    </sapAddress>
    </xsl:template>
    <xsl:template match="TITLE_P">
    	<title><xsl:value-of select="." /></title>
    </xsl:template>
    <xsl:template match="TITLE_ACA1">
    	<academicTitle><xsl:value-of select="." /></academicTitle>
    </xsl:template>
    <xsl:template match="FIRSTNAME">
    	<sapAddrFirstName><xsl:value-of select="." /></sapAddrFirstName>
    </xsl:template>
    <xsl:template match="LASTNAME">
    	<sapAddrLastName><xsl:value-of select="." /></sapAddrLastName>
    </xsl:template>
    <xsl:template match="PREFIX1">
    	<namePrefix><xsl:value-of select="." /></namePrefix>
    </xsl:template>
    <xsl:template match="NAMEFORMAT">
    	<nameFormat><xsl:value-of select="." /></nameFormat>
    </xsl:template>
    <xsl:template match="NAMCOUNTRY">
    	<nameFormatRuleCountry><xsl:value-of select="." /></nameFormatRuleCountry>
    </xsl:template>
    <xsl:template match="LANGU_ISO">
    	<isoLanguage><xsl:value-of select="." /></isoLanguage>
    </xsl:template>
    <xsl:template name="ADDRESS_LANGU">
   		<xsl:param name="value" />
    	<language><xsl:value-of select="$value" /></language>
    </xsl:template>
    <xsl:template match="SORT1_P">
    	<searchSortTerm><xsl:value-of select="." /></searchSortTerm>
    </xsl:template>
    <xsl:template match="DEPARTMENT">
    	<department><xsl:value-of select="." /></department>
    </xsl:template>
    <xsl:template match="FUNCTION">
    	<function><xsl:value-of select="." /></function>
    </xsl:template>
    <xsl:template match="BUILDING_P">
    	<buildingNumber><xsl:value-of select="." /></buildingNumber>
    </xsl:template>
    <xsl:template match="FLOOR_P">
    	<buildingFloor><xsl:value-of select="." /></buildingFloor>
    </xsl:template>
    <xsl:template match="ROOM_NO_P">
    	<roomNumber><xsl:value-of select="." /></roomNumber>
    </xsl:template>
    <xsl:template match="NAME">
    	<name><xsl:value-of select="." /></name>
    </xsl:template>
    <xsl:template match="NAME_2">
    	<name2><xsl:value-of select="." /></name2>
    </xsl:template>
    <xsl:template match="NAME_3">
    	<name3><xsl:value-of select="." /></name3>
    </xsl:template>
    <xsl:template match="NAME_4">
    	<name4><xsl:value-of select="." /></name4>
    </xsl:template>
    <xsl:template match="CITY">
    	<city><xsl:value-of select="." /></city>
    </xsl:template>
    <xsl:template match="POSTL_COD1">
    	<postCode><xsl:value-of select="." /></postCode>
    </xsl:template>
    <xsl:template match="POSTL_COD2">
    	<poBoxPostCode><xsl:value-of select="." /></poBoxPostCode>
    </xsl:template>
    <xsl:template match="PO_BOX">
    	<poBox><xsl:value-of select="." /></poBox>
    </xsl:template>
    <xsl:template match="STREET">
    	<street><xsl:value-of select="." /></street>
    </xsl:template>
    <xsl:template match="STREET_NO">
    	<streetNumber><xsl:value-of select="." /></streetNumber>
    </xsl:template>
    <xsl:template match="HOUSE_NO">
    	<houseNumber><xsl:value-of select="." /></houseNumber>
    </xsl:template>
    <xsl:template match="COUNTRY">
    	<country><xsl:value-of select="." /></country>
    </xsl:template>
    <xsl:template match="COUNTRYISO">
    	<countryIso><xsl:value-of select="." /></countryIso>
    </xsl:template>
    <xsl:template match="REGION">
    	<region><xsl:value-of select="." /></region>
    </xsl:template>
    <xsl:template match="TIME_ZONE">
    	<timeZone><xsl:value-of select="." /></timeZone>
    </xsl:template>
    <xsl:template match="TEL1_NUMBR">
    	<primaryPhoneNumber><xsl:value-of select="." /></primaryPhoneNumber>
    </xsl:template>
    <xsl:template match="TEL1_EXT">
    	<primaryPhoneExtension><xsl:value-of select="." /></primaryPhoneExtension>
    </xsl:template>
    <xsl:template match="FAX_NUMBER">
    	<primaryFaxNumber><xsl:value-of select="." /></primaryFaxNumber>
    </xsl:template>
    <xsl:template match="FAX_EXTENS">
    	<primaryFaxExtension><xsl:value-of select="." /></primaryFaxExtension>
    </xsl:template>
	<!-- END sapAddress -->    
    
    <!-- BEGIN sapCompany -->
    <xsl:template match="COMPANY" >
   		<xsl:apply-templates select="./COMPANY" />
    </xsl:template>
    <xsl:template match="COMPANY/COMPANY">
    	<sapCompany>
    		<companyNameKey><xsl:value-of select="." /></companyNameKey>
    	</sapCompany>
    </xsl:template>
	<!-- END sapCompany -->    
    
    <!-- BEGIN sapDefaults -->
    <xsl:template match="DEFAULTS" >
    	<sapDefaults>
	    	<xsl:apply-templates select="./STCOD" />
	    	<xsl:apply-templates select="./SPLD" />
	    	<xsl:apply-templates select="./SPLG" />
	    	<xsl:apply-templates select="./SPDB" />
	    	<xsl:apply-templates select="./DATFM" />
	    	<xsl:apply-templates select="./DCPFM" />
			<xsl:if test="./LANGU">
	    		<xsl:call-template name="DEFAULTS_LANGU">
	    			<xsl:with-param name="value" select="./LANGU" />
	    		</xsl:call-template>
	    	</xsl:if>
	    	<xsl:apply-templates select="./CATTKENNZ" />
	    	<xsl:apply-templates select="./KOSTL" />
	    </sapDefaults>
    </xsl:template>
    <xsl:template match="STCOD">
    	<startMenu><xsl:value-of select="." /></startMenu>
    </xsl:template>
    <xsl:template match="SPLD">
    	<outputDevice><xsl:value-of select="." /></outputDevice>
    </xsl:template>
    <xsl:template match="SPLG">
    	<printTimeAndDate><xsl:value-of select="." /></printTimeAndDate>
    </xsl:template>
    <xsl:template match="SPDB">
    	<printDelete><xsl:value-of select="." /></printDelete>
    </xsl:template>
    <xsl:template match="DATFM">
    	<dateFormat><xsl:value-of select="." /></dateFormat>
    </xsl:template>
    <xsl:template match="DCPFM">
    	<decimalFormat><xsl:value-of select="." /></decimalFormat>
    </xsl:template>
    <xsl:template name="DEFAULTS_LANGU">
    	<xsl:param name="value" />
    	<logonLanguage><xsl:value-of select="$value" /></logonLanguage>
    </xsl:template>
    <xsl:template match="CATTKENNZ">
    	<cattTestStatus><xsl:value-of select="." /></cattTestStatus>
    </xsl:template>
    <xsl:template match="KOSTL">
    	<costCenter><xsl:value-of select="." /></costCenter>
    </xsl:template>
    <!-- END sapDefaults -->
    
    <!-- BEGIN sapLogonData -->
    <xsl:template match="LOGONDATA" >
    	<sapLogonData>
	    	<xsl:apply-templates select="./GLTGV" />
	    	<xsl:apply-templates select="./GLTGB" />
	    	<xsl:apply-templates select="./USTYP" />
	    	<xsl:apply-templates select="./CLASS" />
	    	<xsl:apply-templates select="./ACCNT" />
	    	<xsl:apply-templates select="./TZONE" />
	    	<xsl:apply-templates select="./LTIME" />
	    	<xsl:apply-templates select="./CODVN" />
	    </sapLogonData>
    </xsl:template>
    <xsl:template match="GLTGV">
    	<validFromDate><xsl:value-of select="." /></validFromDate>
    </xsl:template>
    <xsl:template match="GLTGB">
    	<validToDate><xsl:value-of select="." /></validToDate>
    </xsl:template>
    <xsl:template match="USTYP">
    	<userType><xsl:value-of select="." /></userType>
    </xsl:template>
    <xsl:template match="CLASS">
    	<userGroup><xsl:value-of select="." /></userGroup>
    </xsl:template>
    <xsl:template match="ACCNT">
    	<accountId><xsl:value-of select="." /></accountId>
    </xsl:template>
    <xsl:template match="TZONE">
    	<timeZone><xsl:value-of select="." /></timeZone>
    </xsl:template>
    <xsl:template match="LTIME">
    	<lastLogonTime><xsl:value-of select="." /></lastLogonTime>
    </xsl:template>
    <xsl:template match="CODVN">
    	<codeVerEncryption><xsl:value-of select="." /></codeVerEncryption>
    </xsl:template>
    <!-- END sapLogonData -->
    
    <!-- BEGIN sapSncData -->
    <xsl:template match="SNC" >
    	<sapSncData>
	    	<xsl:apply-templates select="./PNAME" />
	    	<xsl:apply-templates select="./GUIFLAG" />
	    </sapSncData>
    </xsl:template>
    <xsl:template match="PNAME">
    	<printableName><xsl:value-of select="." /></printableName>
    </xsl:template>
    <xsl:template match="GUIFLAG">
    	<allowUnsecure><xsl:value-of select="." /></allowUnsecure>
    </xsl:template>
    <!-- END sapSncData -->

    
    <!-- BEGIN sapGroupList -->
    <xsl:template match="GROUPS">
    	<xsl:if test="./item">
	    	<sapGroupList>
		    	<xsl:apply-templates select="./item" />    		
	    	</sapGroupList>
    	</xsl:if>
	
    </xsl:template>
    <xsl:template match="GROUPS/item">
   		<group>
    		<xsl:apply-templates select="./USERGROUP" />    		
    	</group>
   	</xsl:template>
    <xsl:template match="USERGROUP">
   		<name><xsl:value-of select="." /></name>
    </xsl:template>
	<!-- END sapGroupList -->    
    
    <!-- BEGIN sapParameterList -->
    <xsl:template match="PARAMETER">
    	<xsl:if test="./item">
	    	<sapParameterList>
		    	<xsl:apply-templates select="./item" />
	    	</sapParameterList>
    	</xsl:if>
	
    </xsl:template>
    <xsl:template match="PARAMETER/item">
   		<parameter>
    		<xsl:apply-templates select="./PARID" />
    		<xsl:apply-templates select="./PARVA" />
    	</parameter>
   	</xsl:template>
    <xsl:template match="PARID">
   		<parameterId><xsl:value-of select="." /></parameterId>
    </xsl:template>
    <xsl:template match="PARVA">
   		<parameterValue><xsl:value-of select="." /></parameterValue>
    </xsl:template>
	<!-- END sapParameterList -->    

    
    <!-- BEGIN sapUserEmailAddressList -->
    <xsl:template match="ADDSMTP">
    	<xsl:if test="./item">
	    	<sapUserEmailAddressList>
		    	<xsl:apply-templates select="./item" />
	    	</sapUserEmailAddressList>
    	</xsl:if>
	
    </xsl:template>
    <xsl:template match="ADDSMTP/item">
   		<email>
    		<xsl:apply-templates select="./E_MAIL" />
    		<xsl:apply-templates select="./STD_NO" />
    		<xsl:apply-templates select="./HOME_FLAG" />
    		<xsl:apply-templates select="./CONSNUMBER" />
    	</email>
   	</xsl:template>
    <xsl:template match="E_MAIL">
   		<smtpAddress><xsl:value-of select="." /></smtpAddress>
    </xsl:template>
    <xsl:template match="STD_NO">
   		<defaultNumber><xsl:value-of select="." /></defaultNumber>
    </xsl:template>
    <xsl:template match="HOME_FLAG">
   		<isHomeAddress><xsl:value-of select="." /></isHomeAddress>
    </xsl:template>
    <xsl:template match="CONSNUMBER">
   		<sequenceNumber><xsl:value-of select="." /></sequenceNumber>
    </xsl:template>
	<!-- END sapUserEmailAddressList -->    


    
    <!-- BEGIN sapRoleList -->
    <xsl:template match="ACTIVITYGROUPS">
    	<sapRoleList>
	    	<xsl:apply-templates select="./item" />    		
    	</sapRoleList>
	
    </xsl:template>
    <xsl:template match="ACTIVITYGROUPS/item">
   		<role>
    		<xsl:apply-templates select="./AGR_NAME" />    		
    		<xsl:apply-templates select="./FROM_DAT" />    		
    		<xsl:apply-templates select="./TO_DAT" />    		
    	</role>
   	</xsl:template>
    <xsl:template match="AGR_NAME">
   		<name><xsl:value-of select="." /></name>
    </xsl:template>
    <xsl:template match="FROM_DAT">
   		<validFromDate><xsl:value-of select="." /></validFromDate>
    </xsl:template>
    <xsl:template match="TO_DAT">
   		<validToDate><xsl:value-of select="." /></validToDate>
    </xsl:template>
	<!-- END sapRoleList -->    
     
    <!-- BEGIN sapProfileList -->
    <xsl:template match="PROFILES">
    	<sapProfileList>
	    	<xsl:apply-templates select="./item" />    		
    	</sapProfileList>
    </xsl:template>
    <xsl:template match="PROFILES/item">
   		<profile>
    		<xsl:apply-templates select="./BAPIPROF" />    		
    	</profile>
   	</xsl:template>
    <xsl:template match="BAPIPROF">
   		<name><xsl:value-of select="." /></name>
    </xsl:template>
	<!-- END sapProfileList -->    
    
</xsl:stylesheet>
