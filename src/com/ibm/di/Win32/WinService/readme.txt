HOW TO BUILD:
Use BAT file 'build.bat' to create the ibmdiservice.exe.
To build a 64-bit version of the exe you will need to edit the BAT file. More instructions can be found in it.
Upon execution the BAT file creates two sub-folders: bin/ (or bin64) for the service's exe, and obj/ (or obj64) for the build files.

FILES:
The files present in this folder are (17 totally):
 -	bin/ibmdiservice.exe	:	the 32-bit service wrapper
 -	bin/ibmdiservice.pdb	:	the PDB file for debugging the 32-bit service
 -	bin64/ibmdiservice.exe	:	the 64-bit service wrapper
  -	bin64/ibmdiservice.pdb	:	the PDB file for debugging the 64-bit service
 -	IBMDIService.h	:	header file
 -	NTServApp.h		:	header file
 -	NTService.h		:	header file
 -	jni.h			:	header file needed for JNI
 -	jniport.h		:	header file needed for JNI
 -	IBMDIService.cpp:	source file
 -	NTServApp.cpp	:	source file
 -	NTService.cpp	:	source file
 -	NTServMsg.mc	:	a plain text file containing messages written by the service to the event log; it is parsed by the "mc" utility to generate a header and a resource file
 -	build.bat		:	the script used for building the executables
 -	Makefile		:	the instructions for building the 32-bit exe
 -	Makefile64		:	the instructions for building the 64-bit exe
 -	readme.txt		:	this file
