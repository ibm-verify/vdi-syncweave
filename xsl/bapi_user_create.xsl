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
    
    <xsl:template match="User">
    	<xsl:if test="./sapUserName">
		    <BAPI_USER_CREATE>
		    	<xsl:apply-templates select="./sapUserName" />
	    		<xsl:apply-templates select="./sapUserPassword" />
		    	<xsl:apply-templates select="./sapUserAlias" />	    	
		    	<xsl:apply-templates select="./sapAddress" />	    	
		    	<xsl:apply-templates select="./sapCompany" />	    	
		    	<xsl:apply-templates select="./sapDefaults" />	    	
		    	<xsl:apply-templates select="./sapLogonData" />	    	
		    	<xsl:apply-templates select="./sapSncData" />	    	
		    	<xsl:apply-templates select="./sapUserGroupList" />	    	
			    <xsl:apply-templates select="./sapParameterList" />
			    <xsl:apply-templates select="./sapUserEmailAddressList" />
			</BAPI_USER_CREATE>
		</xsl:if>
    </xsl:template>
    
    <!-- BEGIN Username parameter -->
    <xsl:template match="sapUserName">
    	<USERNAME><xsl:value-of select="." /></USERNAME>
    </xsl:template>
    <!-- END Username parameter -->
    
    <!-- BEGIN Password structure parameter -->
    <xsl:template match="sapUserPassword">
		<PASSWORD>
			<BAPIPWD><xsl:value-of select="." /></BAPIPWD>
		</PASSWORD>
    </xsl:template>
    <!-- END Password structure parameter -->
    
    <!-- BEGIN Alias structure parameter -->
    <xsl:template match="sapUserAlias">
    	<xsl:if test="./aliasName">
    		<ALIAS>
    			<USERALIAS>
    				<xsl:value-of select="./aliasName" />
    			</USERALIAS>
    		</ALIAS>
    	</xsl:if>
    </xsl:template>
    <!-- END Alias structure parameter -->
    
    <!-- BEGIN Address Structure parameter -->
    <xsl:template match="sapAddress">
    	<ADDRESS>
			<xsl:apply-templates select="./title" />
			<xsl:apply-templates select="./academicTitle" />
    		<xsl:apply-templates select="./firstName" />
    		<xsl:apply-templates select="./lastName" />
    		<xsl:apply-templates select="./namePrefix" />
    		<xsl:apply-templates select="./nameFormat" />
    		<xsl:apply-templates select="./nameFormatRuleCountry" />
    		<xsl:apply-templates select="./isoLanguage" />
    		<xsl:apply-templates select="./language" />
    		<xsl:apply-templates select="./searchSortTerm" />
    		<xsl:apply-templates select="./department" />
    		<xsl:apply-templates select="./function" />
    		<xsl:apply-templates select="./buildingNumber" />
    		<xsl:apply-templates select="./buildingFloor" />
    		<xsl:apply-templates select="./roomNumber" />
			<xsl:if test="./name">
	    		<xsl:call-template name="addressName">
	    			<xsl:with-param name="value" select="./name" />
	    		</xsl:call-template>
	    	</xsl:if>
    		<xsl:apply-templates select="./name2" />
    		<xsl:apply-templates select="./name3" />
    		<xsl:apply-templates select="./name4" />
    		<xsl:apply-templates select="./city" />
    		<xsl:apply-templates select="./postCode" />
    		<xsl:apply-templates select="./poBoxPostCode" />
    		<xsl:apply-templates select="./poBox" />
    		<xsl:apply-templates select="./street" />
    		<xsl:apply-templates select="./streetNumber" />
    		<xsl:apply-templates select="./houseNumber" />
    		<xsl:apply-templates select="./country" />
    		<xsl:apply-templates select="./countryIso" />
    		<xsl:apply-templates select="./region" />
			<xsl:if test="./timeZone">
	    		<xsl:call-template name="addressTimeZone">
	    			<xsl:with-param name="value" select="./timeZone" />
	    		</xsl:call-template>
	    	</xsl:if>
    		<xsl:apply-templates select="./primaryPhoneNumber" />
    		<xsl:apply-templates select="./primaryPhoneExtension" />
    		<xsl:apply-templates select="./primaryFaxNumber" />
    		<xsl:apply-templates select="./primaryFaxExtension" />
    	</ADDRESS>	    	
    </xsl:template>
    <!-- END Address Structure parameter-->

    <!-- BEGIN Address Structure Fields -->    
    <xsl:template match="title">
    	<TITLE_P><xsl:value-of select="." /></TITLE_P>
    </xsl:template>
    <xsl:template match="academicTitle">
    	<TITLE_ACA1><xsl:value-of select="." /></TITLE_ACA1>
    </xsl:template>    
    <xsl:template match="firstName">
    	<FIRSTNAME><xsl:value-of select="." /></FIRSTNAME>
    </xsl:template>    
    <xsl:template match="lastName">
    	<LASTNAME><xsl:value-of select="." /></LASTNAME>
    </xsl:template>
    <xsl:template match="namePrefix">
    	<PREFIX1><xsl:value-of select="." /></PREFIX1>
    </xsl:template>
    <xsl:template match="nameFormat">
    	<NAMEFORMAT><xsl:value-of select="." /></NAMEFORMAT>
    </xsl:template>
    <xsl:template match="nameFormatRuleCountry">
    	<NAMCOUNTRY><xsl:value-of select="." /></NAMCOUNTRY>
    </xsl:template>
    <xsl:template match="isoLanguage">
    	<LANGU_ISO><xsl:value-of select="." /></LANGU_ISO>
    </xsl:template>
    <xsl:template match="language">
    	<LANGU><xsl:value-of select="." /></LANGU>
    </xsl:template>
    <xsl:template match="searchSortTerm">
    	<SORT1_P><xsl:value-of select="." /></SORT1_P>
    </xsl:template>
    <xsl:template match="department">
    	<DEPARTMENT><xsl:value-of select="." /></DEPARTMENT>
    </xsl:template>
    <xsl:template match="function">
    	<FUNCTION><xsl:value-of select="." /></FUNCTION>
    </xsl:template>
    <xsl:template match="buildingNumber">
    	<BUILDING_P><xsl:value-of select="." /></BUILDING_P>
    </xsl:template>
    <xsl:template match="buildingFloor">
    	<FLOOR_P><xsl:value-of select="." /></FLOOR_P>
    </xsl:template>    
    <xsl:template match="roomNumber">
    	<ROOM_NO_P><xsl:value-of select="." /></ROOM_NO_P>
    </xsl:template>
    <xsl:template name="addressName">
    	<xsl:param name="value" />
    	<NAME><xsl:value-of select="$value" /></NAME>
    </xsl:template>
    <xsl:template match="name2">
    	<NAME_2><xsl:value-of select="." /></NAME_2>
    </xsl:template>
    <xsl:template match="name3">
    	<NAME_3><xsl:value-of select="." /></NAME_3>
    </xsl:template>
    <xsl:template match="name4">
    	<NAME_4><xsl:value-of select="." /></NAME_4>
    </xsl:template>
    <xsl:template match="city">
    	<CITY><xsl:value-of select="." /></CITY>
    </xsl:template>
    <xsl:template match="postCode">
    	<POSTL_COD1><xsl:value-of select="." /></POSTL_COD1>
    </xsl:template>
    <xsl:template match="poBoxPostCode">
    	<POSTL_COD2><xsl:value-of select="." /></POSTL_COD2>
    </xsl:template>
    <xsl:template match="poBox">
    	<PO_BOX><xsl:value-of select="." /></PO_BOX>
    </xsl:template>
    <xsl:template match="street">
    	<STREET><xsl:value-of select="." /></STREET>
    </xsl:template>
    <xsl:template match="streetNumber">
    	<STREET_NO><xsl:value-of select="." /></STREET_NO>
    </xsl:template>
    <xsl:template match="houseNumber">
    	<HOUSE_NO><xsl:value-of select="." /></HOUSE_NO>
    </xsl:template>
    <xsl:template match="country">
    	<COUNTRY><xsl:value-of select="." /></COUNTRY>
    </xsl:template>
    <xsl:template match="countryIso">
    	<COUNTRYISO><xsl:value-of select="." /></COUNTRYISO>
    </xsl:template>
    <xsl:template match="region">
    	<REGION><xsl:value-of select="." /></REGION>
    </xsl:template>
    <xsl:template name="addressTimeZone">
    	<xsl:param name="value" />
    	<TIME_ZONE><xsl:value-of select="$value" /></TIME_ZONE>
    </xsl:template>
    <xsl:template match="primaryPhoneNumber">
    	<TEL1_NUMBR><xsl:value-of select="." /></TEL1_NUMBR>
    </xsl:template>
    <xsl:template match="primaryPhoneExtension">
    	<TEL1_EXT><xsl:value-of select="." /></TEL1_EXT>
    </xsl:template>
    <xsl:template match="primaryFaxNumber">
    	<FAX_NUMBER><xsl:value-of select="." /></FAX_NUMBER>
    </xsl:template>
    <xsl:template match="primaryFaxExtension">
    	<FAX_EXTENS><xsl:value-of select="." /></FAX_EXTENS>
    </xsl:template>
    <!-- END Address Structure Fields -->    

    <!-- BEGIN Company Structure parameter -->
	<xsl:template match="sapCompany">
    	<xsl:if test="./companyNameKey">
    		<COMPANY>
    			<COMPANY><xsl:value-of select="." /></COMPANY>
    		</COMPANY>
    	</xsl:if>	
	</xsl:template>
    <!-- END Company Structure parameter -->


    <!-- BEGIN Defaults Structure parameter -->
    <xsl:template match="sapDefaults">
    	<DEFAULTS>
			<xsl:apply-templates select="./startMenu" />
			<xsl:apply-templates select="./outputDevice" />
			<xsl:apply-templates select="./printTimeAndDate" />
			<xsl:apply-templates select="./printDelete" />
			<xsl:apply-templates select="./dateFormat" />
			<xsl:apply-templates select="./decimalFormat" />
			<xsl:apply-templates select="./logonLanguage" />
			<xsl:apply-templates select="./cattTestStatus" />
			<xsl:apply-templates select="./costCenter" />
    	</DEFAULTS>
    </xsl:template>
    <!-- END Defaults Structure parameter -->

    <!-- BEGIN Defaults Structure Fields -->    
    <xsl:template match="startMenu">
    	<STCOD><xsl:value-of select="." /></STCOD>
    </xsl:template>
    <xsl:template match="outputDevice">
    	<SPLD><xsl:value-of select="." /></SPLD>
    </xsl:template>
    <xsl:template match="printTimeAndDate">
    	<SPLG><xsl:value-of select="." /></SPLG>
    </xsl:template>
    <xsl:template match="printDelete">
    	<SPDB><xsl:value-of select="." /></SPDB>
    </xsl:template>
    <xsl:template match="dateFormat">
    	<DATFM><xsl:value-of select="." /></DATFM>
    </xsl:template>
    <xsl:template match="decimalFormat">
    	<DCPFM><xsl:value-of select="." /></DCPFM>
    </xsl:template>
    <xsl:template match="logonLanguage">
    	<LANGU><xsl:value-of select="." /></LANGU>
    </xsl:template>
    <xsl:template match="cattTestStatus">
    	<CATTKENNZ><xsl:value-of select="." /></CATTKENNZ>
    </xsl:template>
    <xsl:template match="costCenter">
    	<KOSTL><xsl:value-of select="." /></KOSTL>
    </xsl:template>
    <!-- END Defaults Structure Fields -->    


    <!-- BEGIN Logon Data Structure parameter -->
    <xsl:template match="sapLogonData">
    	<LOGONDATA>
			<xsl:if test="./validFromDate">
				<xsl:call-template name="logondataValidFromDate">
					<xsl:with-param name="value" select="./validFromDate" />
				</xsl:call-template>
			</xsl:if>
			<xsl:if test="./validToDate">
				<xsl:call-template name="logondataValidToDate">
					<xsl:with-param name="value" select="./validToDate" />
				</xsl:call-template>
			</xsl:if>
			<xsl:apply-templates select="./userType" />
			<xsl:apply-templates select="./userGroup" />
			<xsl:apply-templates select="./accountId" />
			<xsl:if test="./timeZone">
				<xsl:call-template name="logondataTimeZone">
					<xsl:with-param name="value" select="./timeZone" />
				</xsl:call-template>
			</xsl:if>
			<xsl:apply-templates select="./lastLogonTime" />
			<xsl:apply-templates select="./codeVerEncryption" />
    	</LOGONDATA>
    </xsl:template>
    <!-- END Logon Data Structure parameter -->

    <!-- BEGIN Logon Data Structure Fields -->    
    <xsl:template name="logondataValidFromDate">
    	<xsl:param name="value" />
    	<GLTGV><xsl:value-of select="$value" /></GLTGV>
    </xsl:template>
    <xsl:template name="logondataValidToDate">
    	<xsl:param name="value" />
    	<GLTGB><xsl:value-of select="$value" /></GLTGB>
    </xsl:template>
    <xsl:template match="userType">
    	<USTYP><xsl:value-of select="." /></USTYP>
    </xsl:template>
    <xsl:template match="userGroup">
    	<CLASS><xsl:value-of select="." /></CLASS>
    </xsl:template>
    <xsl:template match="accountId">
    	<ACCNT><xsl:value-of select="." /></ACCNT>
    </xsl:template>
    <xsl:template name="logondataTimeZone">
    	<xsl:param name="value" />
    	<TZONE><xsl:value-of select="$value" /></TZONE>
    </xsl:template>
    <xsl:template match="lastLogonTime">
    	<LTIME><xsl:value-of select="." /></LTIME>
    </xsl:template>
    <xsl:template match="codeVerEncryption">
    	<CODVN><xsl:value-of select="." /></CODVN>
    </xsl:template>
    <!-- END Logon Data Structure Fields -->    

    <!-- BEGIN Snc Structure parameter -->
    <xsl:template match="sapSncData">
    	<SNC>
			<xsl:apply-templates select="./printableName" />
			<xsl:apply-templates select="./allowUnsecure" />
    	</SNC>
    </xsl:template>
    <!-- END Snc Structure parameter -->

    <!-- BEGIN Snc Structure Fields -->    
    <xsl:template match="printableName">
    	<PNAME><xsl:value-of select="." /></PNAME>
    </xsl:template>
    <xsl:template match="allowUnsecure">
    	<GUIFLAG><xsl:value-of select="." /></GUIFLAG>
    </xsl:template>
    <!-- END Snc Structure Fields -->    

    <!-- BEGIN Group Table parameter -->
    <xsl:template match="sapUserGroupList">
    	<xsl:if test="./group">
    		<xsl:if test="./group/name">
	    		<GROUPS>
	    			<xsl:apply-templates select="./group" />
	    		</GROUPS>
	    	</xsl:if>
    	</xsl:if>
    </xsl:template>
    <!-- END Group Table parameter -->

    <!-- BEGIN Group Table Fields -->    
    <xsl:template match="group">
    	<xsl:if test="./name">
	    	<item>
	    		<xsl:call-template name="groupTableNameField">
	    			<xsl:with-param name="value" select="./name" />
	    		</xsl:call-template>
	    	</item>
    	</xsl:if>
    </xsl:template>
    <xsl:template name="groupTableNameField">
    	<xsl:param name="value" />
    	<USERGROUP><xsl:value-of select="$value" /></USERGROUP>
    </xsl:template>
    <!-- END Group Table Fields -->    

    <!-- BEGIN Parameter Table parameter -->
    <xsl:template match="sapParameterList">
    	<xsl:if test="./parameter">
    		<xsl:if test="./parameter/parameterId">
	    		<PARAMETER>
	    			<xsl:apply-templates select="./parameter" />
	    		</PARAMETER>
	    	</xsl:if>
    	</xsl:if>
    </xsl:template>
    <!-- END Parameter Table parameter -->

    <!-- BEGIN Parameter Table Fields -->    
    <xsl:template match="parameter">
    	<xsl:if test="./parameterId">
	    	<item>
	    		<xsl:apply-templates select="./parameterId" />
	    		<xsl:apply-templates select="./parameterValue" />
	    	</item>
    	</xsl:if>
    </xsl:template>
    <xsl:template match="parameterId">
    	<PARID><xsl:value-of select="." /></PARID>
    </xsl:template>
    <xsl:template match="parameterValue">
    	<PARVA><xsl:value-of select="." /></PARVA>
    </xsl:template>
    <!-- END Parameter Table Fields -->    

    <!-- BEGIN Email ADDSMTP Table parameter -->
    <xsl:template match="sapUserEmailAddressList">
    	<xsl:if test="./email">
    		<xsl:if test="./email/smtpAddress">
	    		<ADDSMTP>
	    			<xsl:apply-templates select="./email" />
	    		</ADDSMTP>
	    	</xsl:if>
    	</xsl:if>
    </xsl:template>
    <!-- END Email ADDSMTP Table parameter -->

    <!-- BEGIN Email ADDSMTP Table Fields -->    
    <xsl:template match="email">
    	<xsl:if test="./smtpAddress">
	    	<item>
	    		<xsl:apply-templates select="./smtpAddress" />
	    		<xsl:apply-templates select="./defaultNumber" />
	    		<xsl:apply-templates select="./isHomeAddress" />
	    		<xsl:apply-templates select="./sequenceNumber" />
	    	</item>
    	</xsl:if>
    </xsl:template>
    <xsl:template match="smtpAddress">
    	<E_MAIL><xsl:value-of select="." /></E_MAIL>
    </xsl:template>
    <xsl:template match="defaultNumber">
    	<STD_NO><xsl:value-of select="." /></STD_NO>
    </xsl:template>
    <xsl:template match="isHomeAddress">
    	<HOME_FLAG><xsl:value-of select="." /></HOME_FLAG>
    </xsl:template>
    <xsl:template match="sequenceNumber">
    	<CONSNUMBER><xsl:value-of select="." /></CONSNUMBER>
    </xsl:template>
    <!-- END Email ADDSMTP Table Fields -->    


</xsl:stylesheet>
