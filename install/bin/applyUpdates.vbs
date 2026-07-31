'
' IBM Confdential 
' PID 5724-K74
'
' . 2025
'

On Error Resume Next

Set objFso      = CreateObject("Scripting.FileSystemObject")
Set scriptShell = CreateObject("WScript.Shell")

Err.Clear

product = "IBM Security Verify Directory Integrator"

' Work out the directory which contains this script.
path        = Wscript.ScriptFullName
Set objFile = objFso.GetFile(path)
folder      = objFso.GetParentFolderName(objFile) 
script_path = objFso.GetAbsolutePathName(".")


' Load the message catalog before we do anything else.
Set catalog = loadMsgCatalog(folder, getLangName())

If Err.Number <> 0 Then
    call displayMessageAndExit("message_catalog_failed", Err.Description)
End If

' Check the command line arguments.
If WScript.Arguments.Count <> 1 Then
    dst=InputBox(getMessage("enter_directory", null), product)
Else
    dst = WScript.Arguments.Item(0)
End If

CURRENT_VERSION = "10.0.0.6"
If Len(dst) = 0 Then
    call displayMessage("no_directory", Null)
    WScript.Quit
End If

' Work out the absolute file name.
dst = objFso.GetAbsolutePathName(dst)

' Work out whether we are being run by cscript.exe
If objFso.GetFileName( WScript.FullName ) = "cscript.exe" Then
    cscript = True
Else
    cscript = False
End If

If cscript Then
    call displayMessage("writing_to", dst)
End If

exists = objFso.FileExists(dst+"\etc\build.properties")

If (exists) then 
	version = getVersion(dst+"\etc")
Else
	msgString="Error - Invalid product install folder. Enter valid product install location."
	If cscript Then
        WScript.Echo msgString
    Else
        MsgBox msgString, 0, product
    End If
	WScript.Quit

End If

result = strComp(version, CURRENT_VERSION)
If (result = 0) then
	WScript.Echo "Installation is up to date. Exiting install."
	WScript.Quit
End If

If (result = 1) then
	WScript.Echo "IVDI " + version + " is found installed. Upgraded version is already installed. Exiting install."
	WScript.Quit
End If

If (result = -1) then
	WScript.Echo "Let's upgrade."	

	p_path = dst + "\maintenance\BACKUP\" + version + "\"
	call buildFullPath(p_path)
	If objFso.FolderExists(dst + "\bin") Then 
		objFso.CopyFolder dst + "\bin", p_path + "\bin"
	End IF
	If objFso.FolderExists(dst + "\etc") Then 
		objFso.CopyFolder dst + "\etc", p_path + "\etc"
	End If
	
	If objFso.FolderExists(dst + "\jars") Then 
		objFso.MoveFolder dst + "\jars", p_path + "\jars"
	End If
	
	If objFso.FolderExists(dst + "\jscript") Then 
		objFso.CopyFolder dst + "\jscript", p_path + "\jscript"
	End If
	If objFso.FolderExists(dst + "\examples") Then 
		objFso.CopyFolder dst + "\examples", p_path + "\examples"
	End If
	If objFso.FolderExists(dst + "\LDAPSync") Then 
		objFso.CopyFolder dst + "\LDAPSync", p_path + "\LDAPSync"	
	End IF
	If objFso.FolderExists(dst + "\libs") Then 
		objFso.CopyFolder dst + "\libs", p_path + "\libs"
	End If
	If objFso.FolderExists(dst + "\osgi") Then 
		objFso.CopyFolder dst + "\osgi", p_path + "\osgi"
	End IF	
	If objFso.FolderExists(dst + "\properties") Then 
		objFso.CopyFolder dst + "\properties", p_path + "\properties"
	End If	
	If objFso.FolderExists(dst + "\pwd_plugins") Then 
		objFso.MoveFolder dst + "\pwd_plugins", p_path + "\pwd_plugins"
	End If	
	If objFso.FolderExists(dst + "\SCIM") Then 
		objFso.CopyFolder dst + "\SCIM", p_path + "\SCIM"
	End If	
	If objFso.FolderExists(dst + "\serverapi") Then 
		objFso.CopyFolder dst + "\serverapi", p_path + "\serverapi"
	End If	
	If objFso.FolderExists(dst + "\tools") Then 
		objFso.CopyFolder dst + "\tools", p_path + "\tools"
	End If
	If objFso.FolderExists(dst + "\win32_service") Then 
		objFso.CopyFolder dst + "\win32_service", p_path + "\win32_service"
	End If	
	If objFso.FolderExists(dst + "\xsd") Then 
		objFso.CopyFolder dst + "\xsd", p_path + "\xsd"
	End If	
	If objFso.FolderExists(dst + "\xsl") Then 
		objFso.CopyFolder dst + "\xsl", p_path + "\xsl"
	End If	
	If objFso.FolderExists(dst + "\XSLT") Then 
		objFso.CopyFolder dst + "\XSLT", p_path + "\XSLT"	
	End If	
	If objFso.FolderExists(dst + "\.registry") Then 
		objFso.CopyFile dst + "\.registry", p_path
	End If
	objFso.CopyFile dst + "\" + "*.bat", p_path
	objFso.CopyFile dst + "\" + "idi*.*", p_path
	objFso.CopyFile dst + "\" + "*.tab", p_path
	objFso.CopyFile dst + "\" + "testserver*.*", p_path	
'End if

call forceConsole(dst)

' Work out the files which are to be unzipped.
unzips = getUnzipFiles(folder)

If Err.Number <> 0 Then
    call displayMessageAndExit("unzip_files_failed", Err.Description)
End If

' Unzip all of the files.
call unzipAllFiles(unzips, folder, dst)



If Err.Number <> 0 Then
    call displayMessageAndExit("unzip_failed", Err.Description)
End If

' Configure SDI.
call configureSDI(dst)

If Err.Number <> 0 Then
    call displayMessageAndExit("config_failed", Err.Description)
End If

' Create the registry file.
call createRegistry(folder, dst)

If Err.Number <> 0 Then
    call displayMessageAndExit("registry_setup_failed", Err.Description)
End If

call updateIbmdiservice(dst)

If Err.Number <> 0 Then
    call displayMessageAndExit("updateIbmdiservice", Err.Description)
End If

call fixGlobalProperties(dst)
If Err.Number <> 0 Then
    call displayMessageAndExit("fixGlobalProperties", Err.Description)
End If

version = getVersion(script_path)
p_path = dst + "\maintenance\BACKUP\" + version
If objFso.FolderExists(p_path) Then 
	objFso.CopyFile p_path + "\etc\derby.properties", dst + "\etc\derby.properties"
	objFso.CopyFile p_path + "\etc\di_castor_mapping.xml", dst + "\etc\di_castor_mapping.xml"
	objFso.CopyFile p_path + "\etc\log4j2.xml", dst + "\etc\log4j2.xml"
	objFso.CopyFile p_path + "\etc\global.properties", dst + "\etc\global.properties"	
	'objFso.CopyFile p_path + "\win32_service\ibmdiservice.props", dst + "\win32_service\ibmdiservice.props"
	objFso.DeleteFile dst + "\etc\TDI1000.SYS2"
	'objFso.DeleteFile dst + "\jars\3rdparty\others\castor-core-1.4.1.jar"
	'objFso.DeleteFile dst + "\jars\3rdparty\others\castor-xml-1.4.1.jar"	
	
End if
End If
' Finished.
call displayMessage("complete", Null)

Set objFso      = Nothing
Set objFile     = Nothing
Set scriptShell = Nothing

WScript.Quit


'===========================================================================
' The following function is used to return the current version from the
' build.properties file.

Function getVersion(src)
    Set propFile = objFso.OpenTextFile(src + "\build.properties", 1, False)

    myVersion = "unknown"

    Do While propFile.AtEndOfStream = False
        strLine = Trim(propFile.ReadLine)
	
        If InStr(1, strLine, "version") > 0 Then
	        intEqualPos = InStr(1, strLine, "=", 1)
            If intEqualPos > 0 Then
                myVersion = Trim(Mid(strLine, intEqualPos + 1))
	        End If
        End If
    Loop

    propFile.Close

    getVersion = myVersion
End Function

'===========================================================================
' The following function is used to create the .registry file.

Sub createRegistry(src, dst)
    ' Open the file for writing.
    version = getVersion(src)

    Set objFile = objFso.CreateTextFile(dst + "\.registry", True)

   ' Write the file.
    objFile.Write "<FIXES>" & vbCrLf
    objFile.Write "</FIXES>" & vbCrLf
    objFile.Write "<EDITION>" & vbCrLf
    objFile.Write "   Identity" & vbCrLf
    objFile.Write "</EDITION>" & vbCrLf
    objFile.Write "<LICENSE>" & vbCrLf
    objFile.Write "   Full" & vbCrLf
    objFile.Write "</LICENSE>" & vbCrLf
    objFile.Write "<LEVEL>" & vbCrLf
    objFile.Write "   " + version & vbCrLf
    objFile.Write "</LEVEL>" & vbCrLf
    objFile.Write "<BASE>" & vbCrLf
    objFile.Write "</BASE>" & vbCrLf
    objFile.Write "<SERVER>" & vbCrLf
    objFile.Write "</SERVER>" & vbCrLf

	objFile.Write "<CE>" & vbCrLf
	objFile.Write "</CE>" & vbCrLf
	objFile.Write "<EXAMPLES>" & vbCrLf
	objFile.Write "</EXAMPLES>" & vbCrLf
	objFile.Write "<PLUGINS>" & vbCrLf
	objFile.Write "</PLUGINS>" & vbCrLf

    objFile.Close
End Sub

'===========================================================================
' The following function is used to correct the entries found within the
' global properties file

Sub configureSDI(dst)
    call execCommand("""" + dst + "\bin\tdiSetJavaHome.bat"" """ + dst + "\jvm""", "config_failed")
    call execCommand("""" + dst + "\bin\setDefaultSolDir.bat"" """ + dst + """", "config_failed")
    call execCommand("""" + dst + "\bin\tdiSetBackupDir.bat"" default", "config_failed")    
End Sub

'===========================================================================
' The following function is used to perform a find and replace within the
' specified file.

Sub findAndReplace(filename, search, newString)
    Set inputFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(filename, 1)
    strInputFile = inputFile.ReadAll
    inputFile.Close
    Set inputFile = Nothing

    Set outputFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(filename,2,true)
    outputFile.Write Replace(strInputFile, search, newString)
    outputFile.Close
    Set outputFile = Nothing
End Sub

'===========================================================================
' The following function is used to update the ibmdiservice.props
' properties file

Sub updateIbmdiservice(dst)
    propertiesFile = dst + "\win32_service\ibmdiservice.props"

    call findAndReplace(propertiesFile, "$jvmRoot$", dst + "\jvm")
    call findAndReplace(propertiesFile, "$change$", dst)
	call findAndReplace(propertiesFile, "jvmcmdoptions=", "jvmcmdoptions=-Dlog4j2.configurationFile=etc/log4j2.xml --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-exports=java.base/sun.security.util=ALL-UNNAMED ")
	
End Sub

'===========================================================================
' The following function is used to correct the entries found within the
' global properties file

Sub fixGlobalProperties(dst)
    propertiesFile = dst + "\etc\global.properties"

    call findAndReplace(propertiesFile, "$TDI_SYSTEM_STORE_PORT$", "1527")
    call findAndReplace(propertiesFile, "$TDI_REST_API_PORT$", "1098")
    call findAndReplace(propertiesFile, "$TDI_SERVER_PORT$", "1099")
End Sub

'===========================================================================
' The following function is used to unzip all files in the specified 
' directory to the specified desintation directory

Sub fixJavaPath(dst)
    For Each oFile In objFso.GetFolder(dst + "\jvm").Subfolders
        If InStr(1, oFile, "jdk-17") > 0 Then
            objFso.MoveFolder oFile, dst + "\jvm\jre"
        End If
    Next
End Sub

'===========================================================================
' The following function is used to correct the entries found within the
' pwsync.props file

Sub fixPwdPluginProperties(dst)

    dstPath = Replace(dst, "\", "/")

    call findAndReplace(dst + "\pwd_plugins\sun\pwsync.props", "$change$", dstPath)
    call findAndReplace(dst + "\pwd_plugins\tds\pwsync.props", "$change$", dstPath)
    call findAndReplace(dst + "\pwd_plugins\windows\pwsync.props", "$change$", dstPath)
    call findAndReplace(dst + "\pwd_plugins\windows\registerpwsync.reg", "$change$", Replace(dst, "\", "\\"))
End Sub

'===========================================================================
' The following function is used to work out which files need to be
' unzipped from the specified source directory.

Function getUnzipFiles(src)
    Dim unzips : unzips = Array()

    ' Determine if we are installing the CE or not.
    installCE = StrComp(WScript.ScriptName, "install_ce.vbs") = 0

    For Each oFile In objFso.GetFolder(src).Files
        If installCE Then
            If InStr(1, oFile, ".zip") > 0 And _
                    (InStr(1, oFile, "TDI_") > 0 ) Then 
                ReDim Preserve unzips(UBound(unzips)+1)
                unzips(UBound(unzips)) = oFile.Name
            End If
        Else
            If InStr(1, oFile, ".zip") > 0 And _
                    (InStr(1, oFile, "TDI_Base") > 0 Or _
                     InStr(1, oFile, "TDI_Server") > 0 Or _
                     InStr(1, oFile, "TDI_LUM") > 0 Or _
                     InStr(1, oFile, "TDI_Plugins") > 0 or _
					 InStr(1, oFile, "eclipsece") > 0) Then
                ReDim Preserve unzips(UBound(unzips)+1)
                unzips(UBound(unzips)) = oFile.Name
            End If
        End If
    Next

    getUnzipFiles = unzips
End Function

'===========================================================================
' Make the full path specified by the dst variable.

Sub buildFullPath(dst)
    If Not objFso.FolderExists(dst) Then
        buildFullPath objFso.GetParentFolderName(dst)
        objFso.CreateFolder dst
    End If
End Sub

'===========================================================================
' The following function is used to unzip all files in the specified 
' directory to the specified desintation directory

Sub unzipAllFiles(unzips, src, dst)
    call buildFullPath(dst)

    For Each oFile In unzips
            zipDest = dst

        If cscript Then
            call displayMessage("extracting", oFile)
        End If

        call unzipFile(src, src + "\" + oFile, zipDest)
    Next
End Sub

'===========================================================================
' The following function is used to unzip the specified file to the
' specified directory.

Sub unzipFile(srcDir, zipFile, dst)
    ' If the extraction location does not exist create it.
   ' If NOT objFso.FolderExists(dst) Then
    '    objFso.CreateFolder(dst)
    'End If

    ' Check if zip file name contains "eclipsece"
    Dim extractPath
    If InStr(1, zipFile, "eclipsece", vbTextCompare) > 0 Then
        ' Extract to dst/ce folder for eclipsece files
        extractPath = dst & "\ce"
        ' Create ce folder if it doesn't exist
        If NOT objFso.FolderExists(extractPath) Then
            objFso.CreateFolder(extractPath)
        End If
        
        ' Remove existing com.ibm.di.*.jar and com.ibm.tdi.*.jar files before unzipping
        If objFso.FolderExists(extractPath) Then
            Dim ceFolder, file
            Set ceFolder = objFso.GetFolder(extractPath)
            For Each file In ceFolder.Files
                If (InStr(1, file.Name, "com.ibm.di.", vbTextCompare) = 1 Or _
                    InStr(1, file.Name, "com.ibm.tdi.", vbTextCompare) = 1) And _
                    LCase(objFso.GetExtensionName(file.Name)) = "jar" Then
                    objFso.DeleteFile file.Path, True
                End If
            Next
        End If
    Else
        ' Extract to dst folder for other files
        extractPath = dst
    End If

    call execCommand("""" + srcDir + "\bin\ibm_vdi_unzip.exe"" """ + zipFile + """ """ + extractPath + """", unzip_failed)

End Sub

'===========================================================================
' The following function is used to retrieve the name of the system
' language.

Function getLangName()
    locale = GetLocale()

    language = "en"

    Select Case locale
        ' German (de)
        Case 1031 language = "de"
        Case 3079 language = "de"
        Case 5127 language = "de"
        Case 4103 language = "de"
        Case 2055 language = "de"

        ' Spanish (es)
        Case 1034  language = "es"
        Case 11274 language = "es"
        Case 16394 language = "es"
        Case 13322 language = "es"
        Case 9226  language = "es"
        Case 5130  language = "es"
        Case 7178  language = "es"
        Case 12298 language = "es"
        Case 4106  language = "es"
        Case 18442 language = "es"
        Case 2058  language = "es"
        Case 19466 language = "es"
        Case 6154  language = "es"
        Case 10250 language = "es"
        Case 20490 language = "es"
        Case 15370 language = "es"
        Case 17418 language = "es"
        Case 14346 language = "es"
        Case 8202  language = "es"

        ' French (fr)
        Case 1036 language = "fr"
        Case 2060 language = "fr"
        Case 3084 language = "fr"
        Case 5132 language = "fr"
        Case 4108 language = "fr"

        ' Italian (it)
        Case 1040 language = "it"
        Case 2064 language = "it"

        ' Japanese (ja)
        Case 1041 language = "ja"

        ' Korean (ko)
        Case 1042 language = "ko"

        ' Portugese (pt_BR)
        Case 1046 language = "pt_BR"

        ' Chinese Simplified (zh_CN)
        Case 2052 language = "zh_CN"
        Case 3076 language = "zh_CN"
        Case 4100 language = "zh_CN"

        ' Chinese Traditional (zh_TW)
        Case 1028 language = "zh_TW"

        ' All others - English
        Case Else language = "en"
    End Select

    getLangName = language

End Function

'===========================================================================
' The following function is used to load the message catalog for the
' specified language.

Function loadMsgCatalog(src, lang)

    Set catalog = CreateObject("Scripting.Dictionary")

    catName = src + "\NLS\install_" + lang + ".json"

    Dim objStream, strData

    Set objStream = CreateObject("ADODB.Stream")

    objStream.CharSet = "utf-8"
    objStream.Type = 2
    objStream.Open
    objStream.LoadFromFile(catName)
    objStream.LineSeparator = 10

    Do Until objStream.EOS
        strLine = Trim(objStream.ReadText(-2))

        intEqualPos = InStr(1, strLine, ":", 1)
        If intEqualPos > 0 Then
            elements = Split(strLine, chr(34))
            catalog.Item(elements(1)) = elements(3)
        End If
    Loop

    objStream.Close
    Set objStream = Nothing

    Set loadMsgCatalog = catalog

End Function

'===========================================================================
' The following function is used to return the message from the message
' catalog.

Function getMessage(msg, arg)

    If IsNull(arg) Then
        strFormat = catalog.Item(msg)
    Else
        strFormat = Replace(catalog.Item(msg), "%s", arg)
    End If

    strFormat = Replace(strFormat, "%%", "%")

    If IsEmpty(strFormat) Then
        WScript.Echo "Error: an invalid message was referenced: " + msg
        WScript.Quit
    End If

    getMessage = strFormat

End Function

'===========================================================================
' The following function is used to display a message from the message
' catalog.

Sub displayMessage(msg, arg)

    msgString = getMessage(msg, arg)

    If cscript Then
        WScript.Echo msgString
    Else
        MsgBox msgString, 0, product
    End If

End Sub

'===========================================================================
' The following function is used to display a message from the message
' catalog and then exit.

Sub displayMessageAndExit(msg, arg)
    call displayMessage(msg, arg)

    fileName = scriptShell.ExpandEnvironmentStrings("%Temp%") + "\ibm-svdi.txt"

    Set oFile = objFso.CreateTextFile(fileName, True)

    oFile.WriteLine getMessage(msg, arg)
    oFile.Close

    WScript.Quit

End Sub


'===========================================================================
' The following function is used to force the execution of this script
' from the console.

Sub forceConsole(dst)
    vbsInterpreter = "cscript.exe"

    If not cscript Then
        scriptShell.Run vbsInterpreter + " //NoLogo """ + WScript.ScriptFullName + """ """ + dst + """", 1, true

        fileName = scriptShell.ExpandEnvironmentStrings("%Temp%") + "\ibm-sdi.txt"

        If objFso.FileExists(fileName) Then
            Set iFile = objFso.OpenTextFile(fileName, 1)

            errString = iFile.ReadAll()
            iFile.Close

            objFso.DeleteFile(fileName)

            MsgBox errString, 0, product
        Else
            call displayMessage("complete", Null)
        End If

        WScript.Quit
    End If

End Sub

'===========================================================================
' The following function will execute the specified command, waiting for
' the command to finish.

Sub execCommand(cmd, errMsg)
    Const WshRunning  = 0
    Const WshFailed   = 2
	Dim exec : Set exec = scriptShell.Exec(cmd)

    While exec.Status = WshRunning
        WScript.Sleep 50
    Wend

    If exec.Status = WshFailed Then
        call displayMessageAndExit(errMsg, "\n" + exec.StdErr.ReadAll)
    End If	
End Sub

