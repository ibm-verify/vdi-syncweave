<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan2="http://xml.apache.org/xalan"
                extension-element-prefixes="xalan2"
                version="1.0">
<xsl:output method="text" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:include href="javaText.xsl"/>
<xsl:include href="Validator.xsl"/>

<!--
  *********************************************************************
  *   Licensed materials - Property of IBM
  *   Tivoli Presentation Services
  *   (C) Copyright IBM Corp. 2000, 2004
  *
  *   US Government Users Restricted Rights - Use, duplication, or
  *   disclosure restricted by GSA ADP Schedule Contract with
  *   IBM Corp.
  *********************************************************************
-->

<!-- Get translated strings -->
<xsl:variable name="trans">tivoli_xsl_xlt.xml</xsl:variable>
<xsl:variable name="transExplanation" select="normalize-space(document($trans)/translations/explanation/.)"/>
<xsl:variable name="transAction" select="normalize-space(document($trans)/translations/action/.)"/>
<xsl:variable name="transSystemAction" select="normalize-space(document($trans)/translations/systemaction/.)"/>
<xsl:variable name="transOperatorResponse" select="normalize-space(document($trans)/translations/operatorresponse/.)"/>
<xsl:variable name="transAdminResponse" select="normalize-space(document($trans)/translations/adminresponse/.)"/>
<xsl:variable name="transProgrammerResponse" select="normalize-space(document($trans)/translations/programmerresponse/.)"/>
<xsl:variable name="transLevel3Support" select="normalize-space(document($trans)/translations/level3support/.)"/>
<!--===============================================================================-->
<!--===============================================================================-->
<!--==Sample invocation:===========================================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<!--                                                                               -->
<!-- java org.apache.xalan.xslt.Process -in fwp_mcr_msg.xml.xml                    -->
<!--                                    -xsl "propertiesResourceBundle.xsl"        -->
<!--                                    -param copyright "copyright.xml"           -->
<!--                                    -param tmm "myMessages.tmm"                -->
<!--                                    -param doubleapos <true|false>             -->
<!--                                    -param varonly <true|false>             -->
<!--                                    -out "messages/fwpMcrMessages.properties"  -->
<!--                                                                               -->
<!--  where:                                                                       -->
<!--  -in = The path and name of the XML source file (e.g., fwp_mcr_msg.xml)       -->
<!--  -xsl = The path and name of the xslt style sheet (this file)                 -->
<!--  -param copyright = The name of the file containing copyright information     -->
<!--                     for the header of the resource bundle.                    -->
<!--  -param tmm = The file with validating criteria (valid prefixes and ranges).  -->
<!--               Optional - Only use this parameter when you want to validate.   -->
<!--  -param doubleapos = The convert single apostrophe occurrences in msgText     -->
<!--                      and labelText to double apostrophe when creating Java    -->
<!--                      properties and list resource bundles. (true|false)       -->
<!--                      Optional - The default value is false.                   -->
<!--  -param varonly = When converting single apostrophe occurrences in msgText    -->
<!--                   and labelText to double apostrophe via the doubleapos       -->
<!--                   option, only perform conversion on messages requiring       -->
<!--                   variable substitution. Individual doubleapos options        -->
<!--                   on a given message overwrite this option. (true|false)      -->
<!--                   Optional - The default value is false.                      -->
<!--  -param detailed =         This specifies the path (relative to the location  -->
<!--                            of this file) where the tivoli_xls_xlt.xml file    -->
<!--                            is located.                                        -->
<!--                            Optional - Only use this parameter if you want to  -->
<!--                            add the Explanation and OperatorResponse to the    -->
<!--                            ResourceBundle.                                    -->
<!--  -param explanation =      This is the text which will be appended to the key -->
<!--                            name which contains the Explanation text.          -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       Explanation.                            -->
<!--  -param systemaction =     This is the text which will be appended to the key -->
<!--                            name which contains the SystemAction text.         -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       SystemAction.                           -->
<!--  -param action =           This is the text which will be appended to the key -->
<!--                            name which contains the Action text.               -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       Action.                                 -->
<!--  -param operatorresponse = This is the text which will be appended to the key -->
<!--                            name which contains the OperatorResponse text.     -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       OperatorResponse.                       -->
<!--  -param adminresponse =    This is the text which will be appended to the key -->
<!--                            name which contains the AdminResponse text.        -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       AdminResponse.                          -->
<!--  -param programmerresponse = This is the text which will be appended to the key -->
<!--                            name which contains the ProgrammerResponse text.   -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       ProgrammerResponse.                     -->
<!--  -param level3support =    This is the text which will be appended to the key -->
<!--                            name which contains the level3Support text.        -->
<!--                            Optional - Only use this parameter if want the     -->
<!--                                       ResourceBundle to contain the           -->
<!--                                       Level3Support.                          -->
<!--  -out = The path and the name of the output file.                             -->
<!--         The output file generated is a Java properties file.                  -->
<!--         The output file specified must have a .properties extension.          -->
<!--         Use forward slash "/" as file seperator.                              -->
<!--                                                                               -->
<!--===============================================================================-->
<!--===============================================================================-->
<!--===The output file looks like the following:===================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<!--                                                                               -->
<!--                                                                               -->
<!-- /**************************************************************               -->
<!-- *  (c) Copyright IBM Corp. (Place dates here)                                 -->
<!-- *************************************************************/                -->
<!-- /***********************************************************************      -->
<!-- * This file was generated by TMS generation utility                           -->
<!-- ************************************************************************/     -->
<!-- FWP1503=FWP1503W The help set {0} cannot  be found.                           -->
<!-- FWP1504=FWP1504W The help set {0} cannot  be successfully parsed.             -->
<!--                                                                               -->
<!--                                                                               -->
<!--===============================================================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<xsl:param name="doubleapos"/>
<xsl:param name="copyright"/>
<xsl:param name="tmm"/>
<xsl:param name="detailed"/>
<xsl:param name="explanation"/>
<xsl:param name="systemaction"/>
<xsl:param name="action"/>
<xsl:param name="operatorresponse"/>
<xsl:param name="adminresponse"/>
<xsl:param name="programmerresponse"/>
<xsl:param name="level3support"/>
<xsl:param name="varonly"/>
<xsl:variable name="comment_start">#</xsl:variable>

  <xsl:variable name="versionlevel">
      Filename: propertiesResourceBundle.xsl
      Version: 1.13
      Date: 09/06/02
      Time: 10:18:52
      Release: 1
  </xsl:variable>

<xsl:template match="/">
  <xsl:if test="$tmm">
    <xsl:call-template name="Validate"/>
  </xsl:if>

  <xsl:call-template name="commentCopyright"/>
  <xsl:call-template name="commentGeneratedBy"/>
  <xsl:apply-templates select="TMSSource/Message | TMSSource/Label"/>
</xsl:template>


<!--===============================================================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<!--======Called templates follow:=================================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<!--===============================================================================-->
<!--===============================================================================-->



<!--================================================================-->
<!-- outputEachLine                                                 -->
<!--                                                                -->
<!-- Output a line of copyright text, prefixed with an asterisk     -->
<!--================================================================-->
<xsl:template name="outputEachLine">
  <xsl:param name="crText"/>
  <xsl:value-of select="concat('* ',substring-before($crText,'&#xA;'))"/>
  <xsl:text>&#xa;</xsl:text>
  <xsl:variable name="remainingText" select="substring-after($crText,'&#xA;')"/>
  <xsl:if test="$remainingText">
    <xsl:call-template name="outputEachLine">
      <xsl:with-param name="crText" select="$remainingText"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>



<!--================================================================-->
<!-- Message | Label                                                -->
<!--                                                                -->
<!-- Extract message text for Properties Resource Bundle            -->
<!--================================================================-->
<xsl:template match="Message | Label">
 <xsl:if test= ".//@varFormat = 'both' or .//@varFormat='Both' or
                .//@varFormat = 'java' or .//@varFormat='Java'">
  <!--==============================================================-->
  <!-- If there is a @pgmKey, use it, otherwise, use the ID.        -->
  <!-- And, if we're dealing with a Message (rather than Label),    -->
  <!-- output its ID before outputting the text.                    -->
  <!--==============================================================-->
  <xsl:variable name="keyName">
  <xsl:choose>
    <xsl:when test="MsgText/@pgmKey or LabelText/@pgmKey">
      <xsl:value-of select="MsgText/@pgmKey | LabelText/@pgmKey"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="@ID"/>
    </xsl:otherwise>
  </xsl:choose>
  </xsl:variable>

  <xsl:text>&#xa;</xsl:text>
  <xsl:value-of select="$keyName"/>
  <xsl:text>=</xsl:text>
  <xsl:if test="self::Message/@ID and not(self::Message/@prefix = 'no')">
    <xsl:value-of select="@ID"/><xsl:text> </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="MsgText | LabelText"/>
 <xsl:if test="string-length($detailed)>0 and self::Message and not(NoHelp)">
    <xsl:apply-templates select="Explanation"        mode="explanation"/>
    <xsl:apply-templates select="SystemAction"       mode="explanation"/>
    <xsl:apply-templates select="Action"             mode="explanation"/>
    <xsl:apply-templates select="OperatorResponse"   mode="explanation"/>
    <xsl:apply-templates select="AdminResponse"      mode="explanation"/>
    <xsl:apply-templates select="ProgrammerResponse" mode="explanation"/>
    <xsl:apply-templates select="Level3Support"      mode="explanation"/>
  </xsl:if>

  <!-- add resource for explanation if param is set -->
  <xsl:if test="string-length($explanation)>0 and self::Message and Explanation">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$explanation"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="Explanation"/>
  </xsl:if>

  <!-- add resource for systemaction if param is set -->
  <xsl:if test="string-length($systemaction)>0 and self::Message and SystemAction">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$systemaction"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="SystemAction"/>
  </xsl:if>

  <!-- add resource for action if param is set -->
  <xsl:if test="string-length($action)>0 and self::Message and Action">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$action"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="Action"/>
  </xsl:if>

  <!-- add resource for operatorresponse if param is set -->
  <xsl:if test="string-length($operatorresponse)>0 and self::Message and OperatorResponse">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$operatorresponse"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="OperatorResponse"/>
  </xsl:if>

  <!-- add resource for adminresponse if param is set -->
  <xsl:if test="string-length($adminresponse)>0 and self::Message and AdminResponse">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$adminresponse"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="AdminResponse"/>
  </xsl:if>

  <!-- add resource for programmerresponse if param is set -->
  <xsl:if test="string-length($programmerresponse)>0 and self::Message and ProgrammerResponse">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$programmerresponse"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="ProgrammerResponse"/>
  </xsl:if>

  <!-- add resource for level3support if param is set -->
  <xsl:if test="string-length($level3support)>0 and self::Message and Level3Support">
    <xsl:text>&#xa;</xsl:text>
    <xsl:value-of select="$keyName"/><xsl:value-of select="$level3support"/>
    <xsl:text>=</xsl:text>
    <xsl:apply-templates select="Level3Support"/>
  </xsl:if>

 </xsl:if>
</xsl:template>

<xsl:template match="Explanation" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transExplanation"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

<xsl:template match="SystemAction" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transSystemAction"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

<xsl:template match="Action" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transAction"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

<xsl:template match="OperatorResponse" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transOperatorResponse"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

<xsl:template match="AdminResponse" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transAdminResponse"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

<xsl:template match="ProgrammerResponse" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transProgrammerResponse"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

<xsl:template match="Level3Support" mode="explanation">
  <xsl:text>\n\n</xsl:text>
  <xsl:value-of select="$transLevel3Support"/>
  <xsl:text>: \n</xsl:text>
  <xsl:apply-templates select="*|text()" mode="explanation" />
</xsl:template>

</xsl:stylesheet>
