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
    
    <xsl:variable name="markModified">X</xsl:variable>
    
    <xsl:template match="User">
    	<xsl:if test="./sapUserName">
		    <BAPI_USER_CHANGE>
		    	<xsl:apply-templates select="./sapUserName" />
	    		<xsl:apply-templates select="./sapUserPassword" />
	    		<xsl:apply-templates select="./sapAddress" />
		    	<xsl:apply-templates select="./sapCompany" />	    	
		    	<xsl:apply-templates select="./sapDefaults" />	    	
		    	<xsl:apply-templates select="./sapLogonData" />	    	
		    	<xsl:apply-templates select="./sapSncData" />	    	
		    	<xsl:apply-templates select="./sapUserGroupList" />	    	
			    <xsl:apply-templates select="./sapParameterList" />
			    <xsl:apply-templates select="./sapUserEmailAddressList" />
			</BAPI_USER_CHANGE>
		</xsl:if>
    </xsl:template>
    
    <!-- BEGIN Username parameter -->
    <xsl:template match="sapUserName">
    	<USERNAME><xsl:value-of select="." /></USERNAME>
    </xsl:template>
    <!-- END Username parameter -->
    
    <!-- BEGIN Password parameter -->
    <xsl:template match="sapUserPassword">
    	<PASSWORD>
    		<BAPIPWD><xsl:value-of select="." /></BAPIPWD>
    	</PASSWORD>
    	<PASSWORDX>
    		<BAPIPWD>X</BAPIPWD>
    	</PASSWORDX>
    </xsl:template>
    <!-- END Password parameter -->
    
    <!-- BEGIN Address Structure parameter -->
    <xsl:template match="sapAddress">
    	<ADDRESS>
    		<xsl:if test="./title">	    	
		   		<TITLE_P><xsl:value-of select="./title" /></TITLE_P>
	    	</xsl:if>
    		<xsl:if test="./academicTitle">	    	
		   		<TITLE_ACA1><xsl:value-of select="./academicTitle" /></TITLE_ACA1>
	    	</xsl:if>
    		<xsl:if test="./firstName">	    	
		   		<FIRSTNAME><xsl:value-of select="./firstName" /></FIRSTNAME>
	    	</xsl:if>
    		<xsl:if test="./lastName">	    	
		   		<LASTNAME><xsl:value-of select="./lastName" /></LASTNAME>
	    	</xsl:if>
    		<xsl:if test="./namePrefix">	    	
		   		<PREFIX1><xsl:value-of select="./namePrefix" /></PREFIX1>
	    	</xsl:if>
    		<xsl:if test="./nameFormat">	    	
		   		<NAMEFORMAT><xsl:value-of select="./nameFormat" /></NAMEFORMAT>
	    	</xsl:if>
    		<xsl:if test="./nameFormatRuleCountry">	    	
		   		<NAMCOUNTRY><xsl:value-of select="nameFormatRuleCountry" /></NAMCOUNTRY>
	    	</xsl:if>
    		<xsl:if test="./isoLanguage">	    	
		   		<LANGU_ISO><xsl:value-of select="isoLanguage" /></LANGU_ISO>
	    	</xsl:if>
    		<xsl:if test="./language">	    	
		   		<LANGU><xsl:value-of select="./language" /></LANGU>
	    	</xsl:if>
    		<xsl:if test="./searchSortTerm">	    	
		   		<SORT1><xsl:value-of select="./searchSortTerm" /></SORT1>
	    	</xsl:if>
    		<xsl:if test="./department">	    	
		   		<DEPARTMENT><xsl:value-of select="./department" /></DEPARTMENT>
	    	</xsl:if>
    		<xsl:if test="./function">	    	
		   		<FUNCTION><xsl:value-of select="./function" /></FUNCTION>
	    	</xsl:if>
    		<xsl:if test="./buildingNumber">	    	
		   		<BUILDING_P><xsl:value-of select="./buildingNumber" /></BUILDING_P>
	    	</xsl:if>
    		<xsl:if test="./buildingFloor">	    	
		   		<FLOOR_P><xsl:value-of select="./buildingFloor" /></FLOOR_P>
	    	</xsl:if>
    		<xsl:if test="./roomNumber">	    	
		   		<ROOM_NO_P><xsl:value-of select="./roomNumber" /></ROOM_NO_P>
	    	</xsl:if>
    		<xsl:if test="./name">	    	
		   		<NAME><xsl:value-of select="./name" /></NAME>
	    	</xsl:if>
    		<xsl:if test="./name2">	    	
		   		<NAME_2><xsl:value-of select="./name2" /></NAME_2>
	    	</xsl:if>
    		<xsl:if test="./name3">	    	
		   		<NAME_3><xsl:value-of select="./name3" /></NAME_3>
	    	</xsl:if>
    		<xsl:if test="./name4">	    	
		   		<NAME_4><xsl:value-of select="./name4" /></NAME_4>
	    	</xsl:if>
    		<xsl:if test="./city">	    	
		   		<CITY><xsl:value-of select="./city" /></CITY>
	    	</xsl:if>
    		<xsl:if test="./postCode">	    	
		   		<POSTL_COD1><xsl:value-of select="./postCode" /></POSTL_COD1>
	    	</xsl:if>
    		<xsl:if test="./poBoxPostCode">	    	
		   		<POSTL_COD2><xsl:value-of select="./poBoxPostCode" /></POSTL_COD2>
	    	</xsl:if>
    		<xsl:if test="./poBox">	    	
		   		<PO_BOX><xsl:value-of select="./poBox" /></PO_BOX>
	    	</xsl:if>
    		<xsl:if test="./street">	    	
		   		<STREET><xsl:value-of select="./street" /></STREET>
	    	</xsl:if>
    		<xsl:if test="./streetNumber">	    	
		   		<STREET_NO><xsl:value-of select="./streetNumber" /></STREET_NO>
	    	</xsl:if>
    		<xsl:if test="./houseNumber">	    	
		   		<HOUSE_NO><xsl:value-of select="./houseNumber" /></HOUSE_NO>
	    	</xsl:if>
    		<xsl:if test="./country">	    	
		   		<COUNTRY><xsl:value-of select="./country" /></COUNTRY>
	    	</xsl:if>
    		<xsl:if test="./countryIso">	    	
		   		<COUNTRYISO><xsl:value-of select="./countryIso" /></COUNTRYISO>
	    	</xsl:if>
    		<xsl:if test="./region">	    	
		   		<REGION><xsl:value-of select="./region" /></REGION>
	    	</xsl:if>
    		<xsl:if test="./timeZone">	    	
		   		<TIME_ZONE><xsl:value-of select="./timeZone" /></TIME_ZONE>
	    	</xsl:if>
    		<xsl:if test="./primaryPhoneNumber">	    	
		   		<TEL1_NUMBR><xsl:value-of select="./primaryPhoneNumber" /></TEL1_NUMBR>
	    	</xsl:if>
    		<xsl:if test="./primaryPhoneExtension">	    	
		   		<TEL1_EXT><xsl:value-of select="./primaryPhoneExtension" /></TEL1_EXT>
	    	</xsl:if>
    		<xsl:if test="./primaryFaxNumber">	    	
		   		<FAX_NUMBER><xsl:value-of select="./primaryFaxNumber" /></FAX_NUMBER>
	    	</xsl:if>
    		<xsl:if test="./primaryFaxExtension">	    	
		   		<FAX_EXTENS><xsl:value-of select="./primaryFaxExtension" /></FAX_EXTENS>
	    	</xsl:if>
    	</ADDRESS>
    	<ADDRESSX>
    		<xsl:if test="./title">	    	
		   		<TITLE_P>X</TITLE_P>
	    	</xsl:if>
    		<xsl:if test="./academicTitle">	    	
		   		<TITLE_ACA1>X</TITLE_ACA1>
	    	</xsl:if>
    		<xsl:if test="./firstName">	    	
		   		<FIRSTNAME>X</FIRSTNAME>
	    	</xsl:if>
    		<xsl:if test="./lastName">	    	
		   		<LASTNAME>X</LASTNAME>
	    	</xsl:if>
    		<xsl:if test="./namePrefix">	    	
		   		<PREFIX1>X</PREFIX1>
	    	</xsl:if>
    		<xsl:if test="./nameFormat">	    	
		   		<NAMEFORMAT>X</NAMEFORMAT>
	    	</xsl:if>
    		<xsl:if test="./nameFormatRuleCountry">	    	
		   		<NAMCOUNTRY>X</NAMCOUNTRY>
	    	</xsl:if>
    		<xsl:if test="./isoLanguage">	    	
		   		<LANGU_ISO>X</LANGU_ISO>
	    	</xsl:if>
    		<xsl:if test="./language">	    	
		   		<LANGU>X</LANGU>
	    	</xsl:if>
    		<xsl:if test="./searchSortTerm">	    	
		   		<SORT1>X</SORT1>
	    	</xsl:if>
    		<xsl:if test="./department">	    	
		   		<DEPARTMENT>X</DEPARTMENT>
	    	</xsl:if>
    		<xsl:if test="./function">	    	
		   		<FUNCTION>X</FUNCTION>
	    	</xsl:if>
    		<xsl:if test="./buildingNumber">	    	
		   		<BUILDING_P>X</BUILDING_P>
	    	</xsl:if>
    		<xsl:if test="./buildingFloor">	    	
		   		<FLOOR_P>X</FLOOR_P>
	    	</xsl:if>
    		<xsl:if test="./roomNumber">	    	
		   		<ROOM_NO_P>X</ROOM_NO_P>
	    	</xsl:if>
    		<xsl:if test="./name">	    	
		   		<NAME>X</NAME>
	    	</xsl:if>
    		<xsl:if test="./name2">	    	
		   		<NAME_2>X</NAME_2>
	    	</xsl:if>
    		<xsl:if test="./name3">	    	
		   		<NAME_3>X</NAME_3>
	    	</xsl:if>
    		<xsl:if test="./name4">	    	
		   		<NAME_4>X</NAME_4>
	    	</xsl:if>
    		<xsl:if test="./city">	    	
		   		<CITY>X</CITY>
	    	</xsl:if>
    		<xsl:if test="./postCode">	    	
		   		<POSTL_COD1>X</POSTL_COD1>
	    	</xsl:if>
    		<xsl:if test="./poBoxPostCode">	    	
		   		<POSTL_COD2>X</POSTL_COD2>
	    	</xsl:if>
    		<xsl:if test="./poBox">	    	
		   		<PO_BOX>X</PO_BOX>
	    	</xsl:if>
    		<xsl:if test="./street">	    	
		   		<STREET>X</STREET>
	    	</xsl:if>
    		<xsl:if test="./streetNumber">	    	
		   		<STREET_NO>X</STREET_NO>
	    	</xsl:if>
    		<xsl:if test="./houseNumber">	    	
		   		<HOUSE_NO>X</HOUSE_NO>
	    	</xsl:if>
    		<xsl:if test="./country">	    	
		   		<COUNTRY>X</COUNTRY>
	    	</xsl:if>
    		<xsl:if test="./countryIso">	    	
		   		<COUNTRYISO>X</COUNTRYISO>
	    	</xsl:if>
    		<xsl:if test="./region">	    	
		   		<REGION>X</REGION>
	    	</xsl:if>
    		<xsl:if test="./timeZone">	    	
		   		<TIME_ZONE>X</TIME_ZONE>
	    	</xsl:if>
    		<xsl:if test="./primaryPhoneNumber">	    	
		   		<TEL1_NUMBR>X</TEL1_NUMBR>
	    	</xsl:if>
    		<xsl:if test="./primaryPhoneExtension">	    	
		   		<TEL1_EXT>X</TEL1_EXT>
	    	</xsl:if>
    		<xsl:if test="./primaryFaxNumber">	    	
		   		<FAX_NUMBER>X</FAX_NUMBER>
	    	</xsl:if>
    		<xsl:if test="./primaryFaxExtension">	    	
		   		<FAX_EXTENS>X</FAX_EXTENS>
	    	</xsl:if>
    	</ADDRESSX>	    	
    </xsl:template>
    <!-- END Address Structure parameter -->

    <!-- BEGIN Company Structure parameter -->
	<xsl:template match="sapCompany">
    	<xsl:if test="./companyNameKey">
    		<COMPANY>
    			<COMPANY><xsl:value-of select="." /></COMPANY>
    		</COMPANY>
    		<COMPANYX>
    			<COMPANY>X</COMPANY>
    		</COMPANYX>
    	</xsl:if>	
	</xsl:template>
    <!-- END Company Structure parameter -->


    <!-- BEGIN Defaults Structure parameter -->
    <xsl:template match="sapDefaults">
    	<DEFAULTS>
			<xsl:if test="./startMenu">
		    	<STCOD><xsl:value-of select="./startMenu" /></STCOD>
			</xsl:if>
			<xsl:if test="./outputDevice">
		    	<SPLD><xsl:value-of select="./outputDevice" /></SPLD>
			</xsl:if>
			<xsl:if test="./printTimeAndDate">
		    	<SPLG><xsl:value-of select="./printTimeAndDate" /></SPLG>
			</xsl:if>
			<xsl:if test="./printDelete">
		    	<SPDB><xsl:value-of select="./printDelete" /></SPDB>
			</xsl:if>
			<xsl:if test="./dateFormat">
		    	<DATFM><xsl:value-of select="./dateFormat" /></DATFM>
			</xsl:if>
			<xsl:if test="./decimalFormat">
		    	<DCPFM><xsl:value-of select="./decimalFormat" /></DCPFM>
			</xsl:if>
			<xsl:if test="./logonLanguage">
		    	<LANGU><xsl:value-of select="./logonLanguage" /></LANGU>
			</xsl:if>
			<xsl:if test="./cattTestStatus">
		    	<CATTKENNZ><xsl:value-of select="./cattTestStatus" /></CATTKENNZ>
			</xsl:if>
			<xsl:if test="./costCenter">
		    	<KOSTL><xsl:value-of select="./costCenter" /></KOSTL>
			</xsl:if>
    	</DEFAULTS>
    	<DEFAULTSX>
			<xsl:if test="./startMenu">
		    	<STCOD>X</STCOD>
			</xsl:if>
			<xsl:if test="./outputDevice">
		    	<SPLD>X</SPLD>
			</xsl:if>
			<xsl:if test="./printTimeAndDate">
		    	<SPLG>X</SPLG>
			</xsl:if>
			<xsl:if test="./printDelete">
		    	<SPDB>X</SPDB>
			</xsl:if>
			<xsl:if test="./dateFormat">
		    	<DATFM>X</DATFM>
			</xsl:if>
			<xsl:if test="./decimalFormat">
		    	<DCPFM>X</DCPFM>
			</xsl:if>
			<xsl:if test="./logonLanguage">
		    	<LANGU>X</LANGU>
			</xsl:if>
			<xsl:if test="./cattTestStatus">
		    	<CATTKENNZ>X</CATTKENNZ>
			</xsl:if>
			<xsl:if test="./costCenter">
		    	<KOSTL>X</KOSTL>
			</xsl:if>
    	</DEFAULTSX>
    </xsl:template>
    <!-- END Defaults Structure parameter -->

    <!-- BEGIN Logon Data Structure parameter -->
    <xsl:template match="sapLogonData">
    	<LOGONDATA>
			<xsl:if test="./validFromDate">
		    	<GLTGV><xsl:value-of select="./validFromDate" /></GLTGV>
			</xsl:if>
			<xsl:if test="./validToDate">
		    	<GLTGB><xsl:value-of select="./validToDate" /></GLTGB>
			</xsl:if>
			<xsl:if test="./userType">
		    	<USTYP><xsl:value-of select="./userType" /></USTYP>
			</xsl:if>
			<xsl:if test="./userGroup">
		    	<CLASS><xsl:value-of select="./userGroup" /></CLASS>
			</xsl:if>
			<xsl:if test="./accountId">
		    	<ACCNT><xsl:value-of select="./accountId" /></ACCNT>
			</xsl:if>
			<xsl:if test="./timeZone">
		    	<TZONE><xsl:value-of select="./timeZone" /></TZONE>
			</xsl:if>
			<xsl:if test="./lastLogonTime">
		    	<LTIME><xsl:value-of select="./lastLogonTime" /></LTIME>
			</xsl:if>
			<xsl:if test="./codeVerEncryption">
		    	<CODVN><xsl:value-of select="./codeVerEncryption" /></CODVN>
			</xsl:if>
    	</LOGONDATA>
    	<LOGONDATAX>
			<xsl:if test="./validFromDate">
		    	<GLTGV>X</GLTGV>
			</xsl:if>
			<xsl:if test="./validToDate">
		    	<GLTGB>X</GLTGB>
			</xsl:if>
			<xsl:if test="./userType">
		    	<USTYP>x</USTYP>
			</xsl:if>
			<xsl:if test="./userGroup">
		    	<CLASS>X</CLASS>
			</xsl:if>
			<xsl:if test="./accountId">
		    	<ACCNT>X</ACCNT>
			</xsl:if>
			<xsl:if test="./timeZone">
		    	<TZONE>X</TZONE>
			</xsl:if>
			<xsl:if test="./lastLogonTime">
		    	<LTIME>X</LTIME>
			</xsl:if>
			<xsl:if test="./codeVerEncryption">
		    	<CODVN>X</CODVN>
			</xsl:if>
    	</LOGONDATAX>
    </xsl:template>
    <!-- END Logon Data Structure parameter -->


    <!-- BEGIN Snc Structure parameter -->
    <xsl:template match="sapSncData">
    	<SNC>
			<xsl:if test="./printableName">
		    	<PNAME><xsl:value-of select="./printableName" /></PNAME>
		    </xsl:if>
			<xsl:if test="./allowUnsecure">
		    	<GUIFLAG><xsl:value-of select="./allowUnsecure" /></GUIFLAG>
		    </xsl:if>
    	</SNC>
    </xsl:template>
    <!-- END Snc Structure parameter -->


    <!-- BEGIN Group Table parameter -->
    <xsl:template match="sapUserGroupList">
    	<xsl:if test="./group/name">
    		<GROUPS>
    			<!--<xsl:apply-templates select="./group/name" /> -->
				<xsl:for-each select="./group">
			    	<item>
				    	<USERGROUP><xsl:value-of select="./name" /></USERGROUP>
			    	</item>
		    	</xsl:for-each>
		    </GROUPS>
    		<GROUPSX>
		    	<USERGROUP>X</USERGROUP>
    		</GROUPSX>
	    </xsl:if>
    </xsl:template>
    <!-- END Group Table parameter -->

    <!-- BEGIN Group Table Fields 
    <xsl:template match="name">
    	<item>
    		<USERGROUP><xsl:value-of select="." /></USERGROUP>
    	</item>
    </xsl:template> -->
    <!-- END Group Table Fields -->   
    

    <!-- BEGIN Parameter Table parameter -->
    <xsl:template match="sapParameterList">
		<xsl:if test="./parameter/parameterId">
	   		<PARAMETER>
	   			<xsl:for-each select="./parameter">
		   			<item>
						<PARID><xsl:value-of select="./parameterId" /></PARID>
						<PARVA><xsl:value-of select="./parameterValue" /></PARVA>
		   			</item>
	   			</xsl:for-each>
	   		</PARAMETER>
	   		<PARAMETERX>
	   			<PARID>X</PARID>
	   			<PARVA>X</PARVA>
	   		</PARAMETERX>
    	</xsl:if>
    </xsl:template>
    <!-- END Parameter Table parameter -->


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
