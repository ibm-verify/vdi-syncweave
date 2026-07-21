HOW TO BUILD:
Use BAT file 'build.bat' to create the COMProxy.dll. By default this script builds a 32-bit dll, so you 
will need to edit it, to build a 64-bit dll.


FILES:
This folder contains the source and header files of COMProxy.dll (see the list below for more information).
Also present are files 'Makefile' and 'Makefile64' that can be used to build the dll for a 32 or 64-bit 
platform. 
All external files used are located in adks (see the list below for more information).
During the building process an 'obj' folder (or 'obj64' when building a 64-bit dll) will appear, containing 
the compiled source files.
After the building process completes successfully the COMProxy.dll file and the other associated files 
(COMProxy.idb, COMProxy.pdb, COMProxy.exp, COMProxy.lib) will appear in the 'bin' folder 
(or 'bin64' for the 64-bit dll). 

List of source and header files present in this folder (11 totally):
1. COMProxy.cpp - this is the main DLL source file.
2. COMUtil.cpp, COMUtil.h - these files contain the utility functions used by the other files.
3. IDispatch.cpp, IDispatch.h - these files contain all IDispatch functions.
4. IUnknown.cpp, IUnknown.h - these files contain all IUnknown functions.
5. SafeArray.cpp, SafeArray.h - these files contain functions to work with arrays (no implementation!). 
6. VARIANT.cpp, VARIANT.h - these files contain a subset of VARIANT functions.

List of external files needed (3 totally):
1. adks/ibm/jdk/1.x/include/jni.h
2. adks/ibm/jdk/1.x/include/jniport.h
3. adks/ibm/jdk/1.x/include/jniproto_md.h

Microsoft Visual Studio DLLs used (12 totally):
kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib	
shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib 	