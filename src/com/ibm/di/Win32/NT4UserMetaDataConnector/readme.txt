HOW TO BUILD:
Use BAT file 'build.bat' to create the WindowsUsers.dll. By default this script builds a 32-bit dll, so you 
will need to edit it, to build a 64-bit dll.


FILES:
This folder contains the source and header files of WindowsUsers.dll (see the list below). Also present are 
files 'Makefile' and 'Makefile64' that can be used to build the dll for a 32 or 64-bit platform. 
All external files used are located in adks (see the list below for more information).
During the building process an 'obj' folder (or 'obj64' when building a 64-bit dll) will appear, containing 
the compiled source files.
After the building process completes successfully the WindowsUsers.dll file and the other associated files 
(WindowsUsers.idb, WindowsUsers.pdb, WindowsUsers.exp, WindowsUsers.lib) will appear in the 'bin' folder 
(or 'bin64' for the 64-bit dll). 

List of external files needed (3 totally):
1. adks/ibm/jdk/1.x/include/jni.h
2. adks/ibm/jdk/1.x/include/jniport.h
3. adks/ibm/jdk/1.x/include/jniproto_md.h

List of source, header and resource files present in this folder (9 totally):
1. com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData.h - this file contains the declaration of the native functions used in the Java code.
2. NTMetaData.cpp - this is the main source file implementing the native functions used in the Java code.
2. JavaStructures.h, NTMetaData.h, NTMetaDataException.h, Wrapper_LPWSTR.h, JavaStructures.cpp,
   NTMetaDataException.cpp, Wrapper_LPWSTR.cpp - these files contain the functions and structures used by the main source file of the DLL. 

Microsoft Visual Studio DLLs used (12 totally):
kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib 
shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib