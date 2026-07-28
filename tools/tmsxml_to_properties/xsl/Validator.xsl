<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="org.apache.xalan.xslt.extensions.Redirect"
                xmlns:xalan2="http://xml.apache.org/xalan"
                xmlns:str="http://exslt.org/strings"
                extension-element-prefixes="xalan2 xalan str"
                version="1.0">
     
<xsl:output method="text" omit-xml-declaration="yes" indent="yes" encoding="UTF-8" /> 



<!--
  *********************************************************************
  *   Licensed materials - Property of IBM
  *   (C) . 2000, 2001
  *   
  *   US Government Users Restricted Rights - Use, duplication, or
  *   disclosure restricted by GSA ADP Schedule Contract with 
  *   IBM Corp.
  *********************************************************************
-->

<xsl:variable name="versionlevel3">
      Filename Validator.xsl  
      Version 1.1
      Date 09/06/02
      Time: 10:17:54
      Release: 1
    </xsl:variable>


<!--==================================================================================-->
<!--==================================================================================-->
<!--==Sample invocation:==============================================================-->
<!--==================================================================================-->
<!--==================================================================================-->
<!--                                                                                  -->
<!-- These validation templates will be called if the parameter "-param tmm"          -->
<!-- is included when the transforms are run.                                         -->
<!--                                                                                  -->
<!--==================================================================================-->
<!--==================================================================================-->
<!--   The output is written to "xxx.error", where "xxx" is taken from the value      -->
<!--   of "TMSSource name=xxx" in the source message file.)                           -->
<!--==================================================================================-->
<!--                                                                                  -->
<!--  1. Check for hard errors, after which we will stop immediately.                 -->
<!--    No TMSMap/Validator elements in TMM file match the TMSSource element.         -->
<!--                                                                                  -->
<!-- 2. Validate the TMM file itself.                                                 -->
<!--    a. Every productPrefix attribute is valid in matching TMSValidators.          -->
<!--    b. Every minIDNumber <= every maxIDNumber in matching TMSValidators.          -->
<!--    c. No ranges overlap for the same productPrefix in any two TMSValidators.     -->
<!--                                                                                  -->
<!-- 3. Validate the XML file.                                                        -->
<!--    a. Correct version number.                                                    -->
<!--    b. No empty elements (TMSSource, Copyright, LabelText)                        -->
<!--    c. No duplicate IDs.                                                          -->
<!--    d. No IDREFs unresolved, and no Label IDs not pointed to by an IDREF.         -->
<!--    e. Every Message ID begins with a string that matches some productPrefix      -->
<!--       attribute in the TMSValidator for this TMSSource. Flag Message IDs that    -->
<!--       don't. (note: Label IDs have no validity criteria so don't check them)     -->
<!--    f. For Message IDs that do match a productPrefix attribute we can tell        -->
<!--       unambiguously what the numeric part of the Message ID is, so validate it   -->
<!--       for being entirely numeric and within the range specified by the TMM file. -->
<!--       (note: Label IDs have no validity criteria so don't check them)            -->
<!--    g. The severity code is I|W|E|A|D|F                                           -->
<!--                                                                                  -->
<!--==================================================================================-->
<!--==================================================================================-->
<!--==================================================================================-->
<!--==================================================================================-->
<!--==================================================================================-->



<!--==================================================================================-->
<!--                                                                                  -->
<!-- Create global variables for frequently used collections                          -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:variable name="TMSSourceName" select="/TMSSource/@name"/>
<xsl:variable name="myValidators" select="document($tmm)/TMSMap/TMSValidator[@sourceName=$TMSSourceName]"/>
<xsl:variable name="productPrefixes" select="$myValidators/TMSRange/@productPrefix"/>
<xsl:variable name="msgIDs" select="/TMSSource/Message/@ID"/>
<xsl:variable name="labelIDs" select="/TMSSource/Label/@ID"/>

<!--================================================================-->
<!-- Validate                                                       -->
<!--                                                                -->
<!-- Check XML file for errors                                      -->
<!--================================================================-->
<xsl:template name="Validate">
  <xsl:call-template name="checkHardErrors"/>        <!-- any hard errors,must stop   -->
  <xalan:write select="concat($TMSSourceName,'.error')">
    <xsl:call-template name="writeHeader"/>          <!-- write header of output file -->
    <xsl:call-template name="validateTMMFile"/>      <!-- Validate the TMSValidators  -->
    <xsl:call-template name="validateTMSFile"/>      <!-- Validate the Message file   -->
 
 <xsl:text>
 *************************************************************************
 
 
 *************************************************************************
 *                        End of messages                                *
 ************************************************************************* 
 </xsl:text>    

  </xalan:write>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!-- checkHardErrors                                                                  -->
<!--                                                                                  -->
<!--  1. Check for hard errors, after which we will stop immediately.                 -->
<!--    No TMSMap/Validator elements in TMM file match the TMSSource element.         -->
<!--    TMSValidator elements with duplicate @sourceNames                             -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="checkHardErrors">
    
<!--    No TMSMap/Validator elements in TMM file match the TMSSource element.    -->
  <xsl:if test="not($myValidators)">
    <xsl:message terminate="yes">
      <xsl:text>
 *************************************************************************
 Error: Can't find a TMSValidator for </xsl:text> <xsl:value-of select="$TMSSourceName"/> 
 <xsl:text>No TMSMap/Validator elements in TMM file match the TMSSource element. 
 ************************************************************************* </xsl:text>
    </xsl:message>
  </xsl:if>
  
  <xsl:for-each select="document($tmm)/TMSMap/TMSValidator">
    <xsl:if test="@sourceName=preceding-sibling::*/@sourceName">
      <xsl:message terminate="yes">
      <xsl:text>
 *************************************************************************
 Error: Duplicate sourceName (</xsl:text><xsl:value-of select="@sourceName"/><xsl:text>).
 Each TMSValidator element must have a unique attribute "sourceName" that matches
 one TMSSource attribute "name". 
 ************************************************************************* </xsl:text>
      </xsl:message>
     </xsl:if>
  </xsl:for-each>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!--                                                                                  -->
<!-- 2. Validate the matching TMM file.                                               -->
<!--    a. Every productPrefix attribute is valid in matching TMSValidator.          -->
<!--    b. Every minIDNumber <= every maxIDNumber in matching TMSValidators.          -->
<!--    c. No ranges overlap for the same productPrefix in any two TMSValidators.     -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="validateTMMFile">
  <xsl:for-each select="$myValidators">
    <xsl:call-template name="validateProductPrefix"/><!-- Validate the productPrefix    -->
    <xsl:call-template name="validateMinMax"/>       <!-- Minimum <= Maximum            -->
  </xsl:for-each>
  <xsl:for-each select="document($tmm)/TMSMap/TMSValidator/TMSRange">
    <xsl:call-template name="overlappingRanges"/>     <!-- Numeric-range overlap         -->
   </xsl:for-each>
</xsl:template>



<!--==================================================================================-->
<!--                                                                                  -->
<!-- validatePrefix                                                                   -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--    a. Every productPrefix attribute is valid (XXXYY).                            -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="validateProductPrefix">
    <xsl:for-each select="TMSRange">
      <xsl:choose>
        <!--============================================================================-->
        <!-- productPrefix is too short                                                 -->
        <!--============================================================================-->
        <xsl:when test="string-length(@productPrefix) &lt; 3">
            <xsl:text>
 *************************************************************************
 Error: The productPrefix (</xsl:text><xsl:value-of select="@productPrefix"/>
 <xsl:text>) for TMSValidator sourceName (</xsl:text><xsl:value-of select="./@sourceName"/>
 <xsl:text>) is less than 3 characters.  All productPrefixes must be 3 characters. 
 </xsl:text>
        </xsl:when>
        <!--============================================================================-->
        <!-- Length OK, validate rest of ID                                             -->
        <!--============================================================================-->
        <xsl:otherwise>
          <!--==========================================================================-->
          <!-- validate 3 byte prefix                                                   -->
          <!--==========================================================================-->
          <xsl:call-template name="validateFirstPart"/>
          <!--==========================================================================-->
          <!-- Any characters after the 3rd byte are the subsystem code:                -->
          <!-- Validate it for all alphabetic characters.                               -->
          <!--==========================================================================-->
          <xsl:if test="string-length(@productPrefix) &gt; 3">
            <xsl:call-template name="validateSubsystem"/>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
</xsl:template>


<!--=================================================================================-->
<!--                                                                                 -->
<!-- validateFirstPart                                                               -->
<!--                                                                                 -->
<!-- Validate the XXX part of XXXYY                                                  --> 
<!--                                                                                 -->
<!--=================================================================================-->
<xsl:template name="validateFirstPart">
  <!--==============================================================================-->
  <!-- Byte 1 is A-J                                                                -->
  <!--==============================================================================-->
  <xsl:if test="translate(substring(@productPrefix, 1, 1), 'ABCDEFGHIJ', '')">
    <xsl:text>
 *************************************************************************
 Error: Invalid prefix.
 The first character of </xsl:text><xsl:value-of select="@productPrefix"/><xsl:text> must be A-J.
 </xsl:text>
  </xsl:if>
  <!--==============================================================================-->
  <!-- Bytes 2 and 3 are A-Z                                                        -->
  <!--==============================================================================-->
  <xsl:if test="translate(substring(@productPrefix, 2, 2), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', '')">
      <xsl:text>
 *************************************************************************
 Error: Invalid prefix.
 The 2nd and 3rd characters of </xsl:text> <xsl:value-of select="@productPrefix"/><xsl:text> must be A-Z.
 </xsl:text>
  </xsl:if>
</xsl:template>
    

<!--===============================================================================-->
<!--                                                                               -->
<!-- validateSubsystem                                                             -->
<!--                                                                               -->
<!--                                                                               -->
<!-- subsystem code, if present, can be 1 or more alphabetic characters            --> 
<!--===============================================================================-->
<xsl:template name="validateSubsystem">
  <xsl:variable name="subsystem" select="substring(@productPrefix,4)"/>
  <!--=============================================================================-->
  <!-- if any character in $subsystem is NOT found in "ABC...", then because the   -->
  <!-- result string '' is shorter than $subsystem, the character in $subsystem    -->
  <!-- is copied to the output unchanged.  Therefore, if anything in the output    -->
  <!-- it means we found an invalid character.                                     -->
  <!--=============================================================================-->
  <xsl:if test="translate($subsystem,'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz','')">
      <xsl:text>
 *************************************************************************
 Error: Invalid prefix for a subcomponent.
 The subcomponent identifier of &quot;</xsl:text><xsl:value-of select="$subsystem"/>
 <xsl:text>&quot; in </xsl:text> <xsl:value-of select="@productPrefix"/> <xsl:text>is invalid. 
 The productPrefix cannot contain any non-alphabetic characters.
 </xsl:text>
  </xsl:if>
</xsl:template>



<!--==================================================================================-->
<!--                                                                                  -->
<!-- validateMinMax                                                                   -->
<!--                                                                                  -->
<!--    b. Every minIDNumber <= every maxIDNumber.                                    -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="validateMinMax">
    <xsl:for-each select="TMSRange">
      <xsl:if test="@maxIDNumber &lt; @minIDNumber">
        <xsl:text>
 *************************************************************************
 Error: Minumum and maximum ranges are incorrect 
 The maximum range </xsl:text><xsl:value-of select="@maxIDNumber"/> <xsl:text> is less 
 than the minimum range </xsl:text><xsl:value-of select="@minIDNumber"/> <xsl:text> for product 
 </xsl:text><xsl:value-of select="@productPrefix"/><xsl:text>.       
      </xsl:text>
      </xsl:if>
    </xsl:for-each>
</xsl:template>

<!--==================================================================================-->
<!--                                                                                  -->
<!-- overlappingRanges                                                                -->
<!--                                                                                  -->
<!-- No Ranges overlap for the same productPrefix attribute in any two TMSValidators  -->
<!-- (note: This validation applies only to the TMM file itself and never even looks  -->
<!-- at the input .xml file)                                                          -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="overlappingRanges">
    <!-- Test to see if this TMSRange has a duplicate productPrefix -->
    <xsl:if test="following::TMSRange[@productPrefix=current()/@productPrefix]">                     
        <xsl:variable name="min" select="@minIDNumber"/>
        <xsl:variable name="max" select="@maxIDNumber"/>
        <xsl:variable name="sourceName" select="parent::TMSValidator/@sourceName"/>
        <xsl:variable name="productPrefix" select="@productPrefix"/>
         
        <!-- Run the following tests for each duplicate productPrefix -->          
        <xsl:for-each select="following::TMSRange[@productPrefix=current()/@productPrefix]">
           <!-- ************************************************************************* -->
           <!--   To see if the current range is outside the other range, check        -->
           <!--    if both (min and max) are either less or more than the              -->
           <!--   minimum or maximum being tested.                                     -->
           <!-- ************************************************************************* -->                
          <xsl:if test="not((number($min) &lt; number(@minIDNumber) and number($max) &lt; number(@minIDNumber)) or (number($min) &gt; number(@maxIDNumber) and number($max) &gt; number(@maxIDNumber)))">
            
    <xsl:text>
 *************************************************************************
 ERROR: The values in two message ID ranges overlap.   
 TMSValidator element sourceName = &quot;</xsl:text><xsl:value-of select="$sourceName"/><xsl:text>&quot; 
 contains the productPrefix &quot;</xsl:text><xsl:value-of select="$productPrefix"/><xsl:text>&quot; range of &quot;</xsl:text>
 <xsl:value-of select="$min"/><xsl:text> to </xsl:text><xsl:value-of select="$max"/><xsl:text>&quot;.
 This overlaps with TMSValidator element sourceName = &quot;</xsl:text><xsl:value-of select="parent::TMSValidator/@sourceName"/><xsl:text>&quot; 
 which has the productPrefix &quot;</xsl:text><xsl:value-of select="@productPrefix"/><xsl:text>&quot; range of &quot;</xsl:text>
  <xsl:value-of select="@minIDNumber"/><xsl:text> to </xsl:text><xsl:value-of select="@maxIDNumber"/><xsl:text>&quot;.
  </xsl:text>
         </xsl:if>
       </xsl:for-each>
   </xsl:if>
</xsl:template>


<!--==================================================================================-->
<!-- ???                                                                                 -->
<!--                                                                                  -->
<!-- 3. Validate the TMS file.                                                        -->
<!--    a. Correct version number.                                                    -->
<!--    b. No empty elements (TMSSource, Copyright, LabelText)                        -->
<!--    c. No duplicate IDs.                                                          -->
<!--    d. No IDREFs unresolved, and no Label IDs not pointed to by any IDREF.        -->
<!--    e. Every Message ID begins with a string that matches some productPrefix      -->
<!--       attribute in the TMSValidator for this TMSSource. Flag Message IDs that    -->
<!--       don't. (note: Label IDs have no validity criteria so don't check them)     -->
<!--    f. For Message IDs that do match a productPrefix attribute we can tell        -->
<!--       unambiguously what the numberic part of the Message ID is, so validate it  -->
<!--       for being entirely numeric and within the range specified by the TMM file. -->
<!--       (note: Label IDs have no validity criteria so don't check them)            -->
<!--    g. The severity code is I|W|E|A|D|F                                           -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="validateTMSFile">
  <xsl:call-template name="checkTMSVersion"/>      <!-- tmsVersion must be 1.0      -->
  <xsl:call-template name="checkEmptyElements"/>   <!-- look for empty elements     -->
  <xsl:call-template name="checkDuplicateIDs"/>    <!-- look for duplicate IDs      -->
  <xsl:call-template name="validateIDREFs"/>       <!-- OperatorChoice IDREFs       -->
  <xsl:call-template name="validateMsgPrefix"/>    <!-- Prefix part matches tmm file-->
  <xsl:for-each select="$msgIDs">
    <xsl:apply-templates 
         select="$productPrefixes[substring(current(),1,string-length(.))=.]"
         mode="checknumeric">
      <xsl:with-param name="msgID" select="."/>
    </xsl:apply-templates>
  </xsl:for-each> 
  
  <xsl:for-each select="$msgIDs">
    <xsl:apply-templates 
         select="$productPrefixes[substring(current(),1,string-length(.))=.]"
         mode="checkrange">
      <xsl:with-param name="msgID" select="."/>
    </xsl:apply-templates>
  </xsl:for-each> 
  <xsl:call-template name="validateSeverity"/>     <!-- Validate severity code      -->
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!--  checknumeric                                                                                -->
<!--                                                                                  -->
<!--                                                                                  --> 
<!--==================================================================================-->
<xsl:template match="@productPrefix" mode="checknumeric">
  <xsl:param name="msgID"/>
  <xsl:variable name="numLength" select="(string-length($msgID) - string-length(.)) - 1"/>
  <xsl:variable name="number" select="substring($msgID,string-length(.)+1,$numLength)"/>              
   <!--=============================================================================-->
   <!-- must be at least 4 bytes                                                    -->
   <!--=============================================================================-->
  <xsl:if test="string-length($number) &lt; 4">
      <xsl:text>
 *************************************************************************
 Error: Message number &quot;</xsl:text><xsl:value-of select="$number"/><xsl:text>&quot; too short in 
 Message </xsl:text><xsl:value-of select="$msgID"/><xsl:text>. It must be minimum 4 bytes.
 </xsl:text>
  </xsl:if>
  <!--=============================================================================-->
  <!-- must not contain any non-numeric data                                       -->
  <!--=============================================================================-->
  <xsl:if test="translate($number,'0123456789','')">
      <xsl:text>
 *************************************************************************
 Error: Message number &quot;</xsl:text><xsl:value-of select="$number"/><xsl:text>&quot; contains 
 non-numeric data in </xsl:text><xsl:value-of select="$msgID"/><xsl:text>.
 </xsl:text>
   </xsl:if>
</xsl:template>



<!--==================================================================================-->
<!--                                                                                  -->
<!-- checkTMSVersion                                                                  -->
<!--                                                                                  -->
<!-- tmsVersion must be 1.0                                                           --> 
<!--==================================================================================-->
<xsl:template name="checkTMSVersion">
  <xsl:if test="not(TMSSource/@tmsVersion='1.0')">
    <xsl:text>
 *************************************************************************
 Error: "tmsVersion" attribute in "TMSSource" element must be 1.0
 </xsl:text>
  </xsl:if>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!-- checkEmptyElements                                                               -->
<!--                                                                                  -->
<!-- 1. TMSSource without Message or Label child.                                     -->
<!-- 2. Label elements without LabelText                                              --> 
<!-- 3. Copyright element can't be empty                                              --> 
<!--==================================================================================-->
<xsl:template name="checkEmptyElements">
  <!--==================================================================================-->
  <!-- 1. TMSSource without Message or Label child.                                     -->
  <!--==================================================================================-->
  <xsl:if test="not(TMSSource/Message | TMSSource/Label)">
    <xsl:text>
 *************************************************************************
 Error: TMSSource must have at least one Message or Label child element
 </xsl:text>
  </xsl:if>
  
  <!--==================================================================================-->
  <!-- 2. Label elements without LabelText                                              --> 
  <!--==================================================================================-->
  <xsl:apply-templates select="TMSSource/Label[not(normalize-space(LabelText))]" mode="empty"/>
  <!--==================================================================================-->
  <!-- 3. Copyright element can't be empty                                              --> 
  <!--==================================================================================-->
  <xsl:if test="not(normalize-space(TMSSource/Copyright))">
    <xsl:text>
 *************************************************************************
 Error: TMSSource must have a non-empty Copyright element.
 </xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template match="TMSSource/Label" mode="empty">
  <xsl:text>
 *************************************************************************
 Error: Label </xsl:text><xsl:value-of select="@ID"/>
  <xsl:text> does not have any LabelText.
  </xsl:text>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!-- checkDuplicateIDs                                                                -->
<!--                                                                                  -->
<!-- Message or Label with ID x already exists                                        -->
<!--==================================================================================-->
<xsl:template name="checkDuplicateIDs">
  <xsl:variable name="allMessages" select="TMSSource/Message | TMSSource/Label"/>
  <xsl:for-each select="$allMessages">
    <xsl:if test="$allMessages[./@ID = current()/@ID and generate-id(.) != generate-id(current())]">
      <xsl:text>
 *************************************************************************
 Error: The ID </xsl:text><xsl:value-of select="@ID"/><xsl:text> has already been used.
 </xsl:text>
    </xsl:if>
  </xsl:for-each>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!-- validateIDREFs (in OperatorChoice)                                               -->
<!--                                                                                  -->
<!--                                                                                  -->
<!-- The Label referred by the 'OperatorChoice'using IDREF= not found                 -->
<!-- The Label element with id '$id' is not referred by any 'OperatorChoice' element  -->
<!--==================================================================================-->
<xsl:template name="validateIDREFs">
  <!--================================================================================-->
  <!-- The Label referred by the 'OperatorChoice'element using IDREF= not found       -->
  <!--================================================================================-->
  <xsl:for-each select="TMSSource/Message/OperatorChoice">
    <xsl:if test="not(/TMSSource/Label/@ID=@IDREF)">
      <xsl:text>
 *************************************************************************
 Error: The Label referred to by IDREF=</xsl:text><xsl:value-of select="@IDREF"/><xsl:text> is not 
 found.
 </xsl:text>
    </xsl:if>
  </xsl:for-each>
  <!--================================================================================-->
  <!-- The Label element with id '$id' is not referred by any OperatorChoice IDREF    -->
  <!--================================================================================-->
  <xsl:for-each select="TMSSource/Label">
    <xsl:if test="not(/TMSSource/Message/OperatorChoice/@IDREF=@ID)">
      <xsl:text>
 *************************************************************************
 Error: The Label with ID=</xsl:text><xsl:value-of select="@ID"/><xsl:text> is not referred 
 to by any OperatorChoice using IDREF.
 </xsl:text>
    </xsl:if>
  </xsl:for-each>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!-- validateMsgPrefix                                                                -->
<!--                                                                                  -->
<!-- For each Message ID, look for a matching productPrefix attribute in myValidators.-->
<!--   If found, add MessageID to variable $matchedIDs                                -->
<!--   If not found, issue an error message for this ID.                              -->
<!-- At the end of loop, return the collection of Message IDs for which a matching    -->
<!-- productPrefix attribute was found.    ??? does this really work                 -->
<!--==================================================================================-->
<xsl:template name="validateMsgPrefix">
  <xsl:for-each select="$msgIDs">
    <xsl:variable name="matchedIDs">
      <xsl:for-each select="$myValidators/TMSRange/@productPrefix">
        <xsl:if test="substring(current(),1,string-length(.))=.">
          <xsl:value-of select="current()"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="not($matchedIDs)">
      <xsl:text>
 *************************************************************************
 Error: The prefix in message </xsl:text><xsl:value-of select="."/><xsl:text> was not found 
 in </xsl:text><xsl:value-of select="$tmm"/><xsl:text>.</xsl:text>
    </xsl:if>
  </xsl:for-each>
  
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!-- validateRange                                                                    -->
<!--                                                                                  -->
<!--                                                                                  -->
<!-- All Message IDs in TMSSource are within the allowed min/max range.               -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="validateRange">
  <xsl:param name="matchedIDs"/>
  <xsl:for-each select="$matchedIDs">
    <xsl:variable name="inrange">  
      <xsl:apply-templates select="$myValidators" mode="range">
        <xsl:with-param name="msgID" select="."/>  
      </xsl:apply-templates>
    </xsl:variable>
    <!--==============================================================================-->
    <!-- If we haven't found the number within any valid range (i.e., nothing has not -->
    <!-- been returned after looking at every matching prefix), then issue an error.  -->
    <!--==============================================================================-->
    <xsl:if test="not($inrange)">
      <xsl:text>
 *************************************************************************
  Error: The number (</xsl:text><xsl:value-of select="."/><xsl:text>is not in a valid range for message.
   </xsl:text>
    </xsl:if>
  </xsl:for-each>
</xsl:template>


<!--==================================================================================-->
<!--                                                                                  -->
<!--  checkrange                                                                      -->
<!--                                                                                  -->
<!--                                                                                  --> 
<!--==================================================================================-->
<xsl:template match="@productPrefix" mode="checkrange">
  <xsl:param name="msgID"/>
  <xsl:variable name="numLength" select="(string-length($msgID) - string-length(.)) - 1"/>
  <xsl:variable name="number" select="substring($msgID,string-length(.)+1,$numLength)"/>              
  <xsl:apply-templates select="$myValidators/TMSRange">
    <xsl:with-param name="msgPrefix" select="."/>  
    <xsl:with-param name="msgNum" select="$number"/>  
  </xsl:apply-templates>
</xsl:template>


<!--=============================================================================-->
<!--                                                                             -->
<!-- TMSRange                                                                    -->
<!--                                                                             -->
<!-- return string "within" if message is within range.                          -->
<!-- return empty string if message is not in range.                             -->
<!--=============================================================================-->
<xsl:template match="TMSRange">
  <xsl:param name="msgPrefix"/>
  <xsl:param name="msgNum"/>

    <xsl:if test="$msgPrefix=@productPrefix">
       <xsl:if test="(number($msgNum) &lt; number(@minIDNumber)) or (number($msgNum) &gt; number(@maxIDNumber))">
          <xsl:text>
 *************************************************************************
  Error: Message is not within range specified in minimum and maximum values.
  Message prefix is &quot;</xsl:text><xsl:value-of select="$msgPrefix"/><xsl:text>&quot;.
  Message number is &quot;</xsl:text><xsl:value-of select="$msgNum"/><xsl:text>&quot;.
  Range is minimum of </xsl:text><xsl:value-of select="@minIDNumber"/><xsl:text> and maximum of </xsl:text><xsl:value-of select="@maxIDNumber"/><xsl:text>.
  </xsl:text>

      </xsl:if>
    </xsl:if>
</xsl:template>

<!--===============================================================================-->
<!--                                                                               -->
<!-- validateSeverity                                                              -->
<!--                                                                               -->
<!-- Validate the I part of XXXYYnnnnI    I,W,E    (A,D,F are obsolete)            --> 
<!--                                                                               -->
<!--===============================================================================-->
<xsl:template name="validateSeverity">
  <xsl:for-each select="$msgIDs">
    <!--=============================================================================-->
    <!-- Severity code is I,W,E,A,D,F                                                -->
    <!--=============================================================================-->
    <xsl:if test="translate(substring(., string-length(.), 1), 'IWEADF', '')">
        <xsl:text>
 *************************************************************************
  Error: Invalid severity code in: </xsl:text><xsl:value-of select="."/><xsl:text> must be I,W,E.
  </xsl:text>
    </xsl:if>
    <!--=============================================================================-->
    <!-- Severity code is A,D,F issue warning for obsolete codes                     -->
    <!--=============================================================================-->
    <xsl:if test="translate(substring(., string-length(.), 1), 'ADF', '')=''">
        <xsl:text>
 *************************************************************************
 Note: Severity code &quot;</xsl:text> <xsl:value-of select="."/><xsl:text>&quot; is obsolete. 
 Use I, W, or E.
 </xsl:text>
    </xsl:if>
  </xsl:for-each>
</xsl:template>
    



<!--=============================================================================-->
<!--                                                                             -->
<!-- getMsgNumber                                                                -->
<!--                                                                             -->
<!-- The Tivoli Message ID format is XXXYY####Z where:                           -->
<!--                                                                             -->
<!--  XXX = 3-character product prefix                                           -->
<!--  YY  = optional subsystem code, from 0 to 2 bytes.                          -->
<!--  #### = msg number (4 bytes)                                                -->
<!--  Z = severity code                                                          -->
<!--                                                                             -->
<!--  So, go to the end of the string and take 4 bytes from before the           -->
<!--  severity code.                                                             -->
<!--=============================================================================-->
<xsl:template name="getMsgNum">
  <xsl:param name="msgID"/>  
  <xsl:value-of select="substring($msgID, string-length($msgID) - 4,4)"/>  
</xsl:template>



<!--==================================================================================-->
<!--                                                                                  -->
<!-- writeHeader                                                                      -->
<!--                                                                                  -->
<!--                                                                                  -->
<!-- The input TMSSource has a corresponding TMSValidator element in the tmm file.    --> 
<!--                                                                                  -->
<!--                                                                                  -->
<!--==================================================================================-->
<xsl:template name="writeHeader">
  <xsl:text>Validating &quot;</xsl:text>
  <xsl:value-of select="$TMSSourceName"/>    
  <xsl:text>&quot; with &quot;</xsl:text>
  <xsl:value-of select="$tmm"/><xsl:text>
  
 This file was generated by:   </xsl:text>
  <xsl:for-each select="str:tokenize($versionlevel,'&#xa;')">
     <xsl:text>&#xa; </xsl:text><xsl:value-of select="."/>
  </xsl:for-each>
<xsl:text>
 which included:   </xsl:text>
   <xsl:for-each select="str:tokenize($versionlevel2,'&#xa;')">
      <xsl:text>&#xa; </xsl:text><xsl:value-of select="."/>
   </xsl:for-each>
<xsl:text>
 and also included:   </xsl:text>
   <xsl:for-each select="str:tokenize($versionlevel3,'&#xa;')">
      <xsl:text>&#xa; </xsl:text><xsl:value-of select="."/>
   </xsl:for-each><xsl:text>
      
 *************************************************************************
 *                        Begin messages                                 *
 *************************************************************************
  
 </xsl:text> 
</xsl:template>

</xsl:stylesheet>
