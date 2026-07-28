<?xml version="1.0" encoding="UTF-8" ?>

<!-- 

/*********************************************************** {COPYRIGHT-TOP} ***
* Licensed materials - Property of IBM
* Tivoli Directory Integrator
*
* (C) . 2006
*
* IBM Corp.
************************************************************ {COPYRIGHT-END} **/

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- 
	Parameters passed in from SDI 
-->
	<xsl:param name="tdiLocale">en</xsl:param>
	<xsl:param name="tdiObjectName" />

	<xsl:output method="html" indent="yes" encoding="UTF-8" />			           

	<!-- version number for this stylesheet -->
	<xsl:variable name="version">1.1</xsl:variable>

<!-- set global variables for translatable terms and images -->
	<xsl:variable name="trans">translation/<xsl:if test="string-length($tdiLocale)>0"><xsl:value-of select="$tdiLocale"/>/</xsl:if>Inheritance.xml</xsl:variable>
<!--  get translatable terms   -->
	<xsl:variable name="reportTitle" select="document($trans)/translation/reportTitle"/>
	<xsl:variable name="connectors" select="document($trans)/translation/connectors"/>
	<xsl:variable name="functions" select="document($trans)/translation/functions"/>
	<xsl:variable name="attributeMaps" select="document($trans)/translation/attributeMaps"/>
	<xsl:variable name="scripts" select="document($trans)/translation/scripts"/>
	<xsl:variable name="parsers" select="document($trans)/translation/parsers"/>
	<xsl:variable name="v_for_version" select="document($trans)/translation/v_for_version"/>
	<xsl:variable name="separator" select="document($trans)/translation/separator"/>

<!-- end of translatable items -->
	

	<!--  couple of variables I can use if I need to output quotemarks or apostrophes
	-->
	<xsl:variable name="apos" select='"&apos;"' />
	<xsl:variable name="quot" select="'&quot;'" />

	<!-- Gonna build a style with these settings further down. Edit here to
		  change the look of the report.
	--> 
	<xsl:variable name="divSettings">position: relative; left: +1em</xsl:variable>
	

	

	<!-- This template is my main(). It matches the tree at the root and initiates
		  all other processing.
	-->
	<xsl:template match="/">
		<!-- First I output some initial HTML text
		-->
		<html>
			<head>
				<title><xsl:value-of select="$reportTitle"/><xsl:text> </xsl:text><xsl:value-of select="$v_for_version"/><xsl:value-of select="$version"/></title>

				<!-- Here are the styles that control the formatting of the report.             
				-->
				<style type="text/css">
			
					div {
					<xsl:value-of select="$divSettings"/>
					}
			
					h1 {
					}
					
					tr.Odd {
							background-color: #CCCCCC;
					}
					tr.Even {
							background-color: #FFFFFF;
					}
					
				</style>  <!--       ==============================      -->
	
			</head>

			<body>
				<h2><xsl:value-of select="$reportTitle"/><xsl:text> </xsl:text><xsl:value-of select="$v_for_version"/><xsl:value-of select="$version"/></h2>
				
				<!-- Here in the <body> section we start applying other templates using XPath
					  expressions to control which nodes are used.
				-->

				<!-- Apply all templates for a "Connector" node found just under a "Folder".
					  This will only happen for Library Connectors. All others are wrapped
					   in an AssemblyLine's section tag, like <ContainerEF name="EntryFeedContainer>
					   and won't match the XPath selection expression. Note the mode setting of
					   "topLevel" which will control which templates get a shot at the selected nodes.

				        And we'll <xsl:sort> them by the name attribute.
				-->
				<h2><xsl:value-of select="$connectors"/></h2>
				
				<xsl:for-each select="//Folder[@name='Connectors']/Connector">
					<xsl:sort select="@name"/>

					<xsl:call-template name="FindParents">
						<xsl:with-param name="folderName">/Connectors/</xsl:with-param>
						<xsl:with-param name="xpathStub" select="/*//Connector"/>
						<xsl:with-param name="cType">Connectors</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>

				<h2><xsl:value-of select="$functions"/></h2>
				
				<xsl:for-each select="//Folder[@name='Functions']/Function">
					<xsl:sort select="@name"/>

					<xsl:call-template name="FindParents">
						<xsl:with-param name="folderName">/Functions/</xsl:with-param>
						<xsl:with-param name="xpathStub" select="/*//Function"/>
						<xsl:with-param name="cType">Functions</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>

				<h2><xsl:value-of select="$attributeMaps"/></h2>
				
				<xsl:for-each select="//Folder[@name='AttributeMaps']/ALMap">
					<xsl:sort select="@name"/>

					<xsl:call-template name="FindParents">
						<xsl:with-param name="folderName">/AttributeMaps/</xsl:with-param>
						<xsl:with-param name="xpathStub" select="/*//ALMap"/>
						<xsl:with-param name="cType">AttributeMaps</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>

				<h2><xsl:value-of select="$parsers"/></h2>
				
				<xsl:for-each select="//Folder[@name='Parsers']/Parser">
					<xsl:sort select="@name"/>

					<xsl:call-template name="FindParents">
						<xsl:with-param name="folderName">/Parsers/</xsl:with-param>
						<xsl:with-param name="xpathStub" select="/*//Parser"/>
						<xsl:with-param name="cType">Parsers</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>

				<h2><xsl:value-of select="$scripts"/></h2>
				
				<xsl:for-each select="//Folder[@name='Scripts']/Script">
					<xsl:sort select="@name"/>

					<xsl:call-template name="FindParents">
						<xsl:with-param name="folderName">/Scripts/</xsl:with-param>
						<xsl:with-param name="xpathStub" select="/*//Connector"/>
						<xsl:with-param name="cType">Scripts</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>


			</body>
		</html>
	</xsl:template>




	<!-- This template matches Folders named "Connectors"
		  Parameters:
					xpathStub				Stub used below to created the descendant XPath
												    selection expression.
					folderName				Name of Library Folder we are working with: "/Connectors/",
													"/Parsers/", "/Scripts/", etc.
					cType						Type of Component
	-->
	<xsl:template name="FindParents">
		<xsl:param name="xpathStub"/>
		<xsl:param name="folderName"/>
		<xsl:param name="cType"/>


		<!--	The qualifiedName is how a Library Component is referenced in the 
				<InheritFrom> tag: the Folder name first, followed by the the component 
				type, e.g. 
													"/Connectors/MyLibraryConnector"
		-->
		<xsl:variable name="qualifiedName"><xsl:value-of select="concat($folderName,@name)"/></xsl:variable>
		
		<!-- This is the XPath expression for locating descendants, which I have defined to look
			   for any nodes of the same type (like <Connector>) which have a sub-node called
			   <InheritFrom> which references the name of the current one.
		-->
		<xsl:variable name="xpathFindDescendants" select="$xpathStub[InheritFrom=$qualifiedName]" />
				
		<!-- First check to see if we have any descendants before outputing anything.
		-->
		<xsl:if test="$xpathFindDescendants">
		
			<!-- If so, mark a new <div>ision, output the component name and then apply
				  templates on all Descendants using the "Descendants" mode (preventing,
				  for example, this template from matching on any of the components found.
				  Tags like <Connector> and <Script> are used both in the Library and in
				  AssemblyLines.
			-->
			<div>
				<!-- Output the name of this library component.
				-->
				<h3><xsl:value-of select="@name"/></h3>
				
				<!-- Now walk through descendants, passing the current descendant node to the output template
				-->
				<div>
					<table>
						<xsl:for-each select="$xpathFindDescendants">
							<xsl:call-template name="OutputDescendant">
								<xsl:with-param name="cType" select="$cType"/>
							</xsl:call-template>
						</xsl:for-each>
					</table>
				</div>
				
			</div> <!-- end of output section for this component type -->
			
		</xsl:if> <!-- test for descendants -->
		
	</xsl:template>

	
	
	
	<xsl:template name="OutputDescendant">
		<xsl:param name="cType"/>
		
		<tr>
			<xsl:if test="position() mod 2 = 0">
				<xsl:attribute name="class">Even</xsl:attribute>
			</xsl:if>
			<xsl:if test="position() mod 2 = 1">
				<xsl:attribute name="class">Odd</xsl:attribute>
			</xsl:if>
			
			<xsl:if test="ancestor::AssemblyLine/@name">
				<td>AssemblyLine</td>
				<td><xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text></td>

				<td><xsl:value-of select="ancestor::AssemblyLine/@name"/></td>
			
				<td><xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text></td>
				<td><xsl:value-of select="@name"/></td>
			</xsl:if>
			<xsl:if test="ancestor::Folder[@name = $cType]">
				<td><xsl:value-of select="$cType"/> Library</td>
				<td><xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text></td>
				<td></td>
				<td><xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;</xsl:text></td>
				<td><xsl:value-of select="@name"/></td>
			</xsl:if>
		</tr>
	</xsl:template>
	

</xsl:stylesheet>
