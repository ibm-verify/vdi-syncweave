# Overview

This directory contains the source code for an unzip program which is used during the SDI install on Windows.  The custom unzip program is required because of performance issues with the standard Windows unzip program (e.g. the standard Windows unzip program takes 3 m 31 sec to extract the IBM Semeru Java zip file, whereas the binary found in this directory takes only 7 sec).

The program itself relies on the [zlib](https://github.com/madler/zlib) library (currently built with v1.3).  The `ibm_vdi_unzip.c` source file has it's origins in the miniunz program, found in the 'contrib/minizip' directory of the zlib source.  The miniunz source code was trimmed down to the bare necessities and tidied up.

# Files

The following files are located in this directory:

|Name|Description
|----|-----------
|Makefile.msc|An MSVC compatible makefile which can be used to build the source.
|README.md|This file.
|ibm\_vdi\_unzip.c|The source file for the unzip program.
|ibm\_vdi\_unzip.exe|The generated executable.

# Building

In order to build the source file you need:

1. Vistual Studio (with the C++ compiler) installed in a Windows environment;
2. The zlib source code, available from [GitHub](https://github.com/madler/zlib).

**Steps**

1. Build the zlib source code from within the win32 directory of the zlib source: `nmake /f Makefile.msc`
2. Set the TOP variable within the Makefile.msc file found within this directory to point to the root of the built zlib source.
3. Build the ibm\_vdi\_unzip.exe binary: `nmake /f Makefile.msc`

The constructed binary should also be signed using the CISO code signing utility.  See the following Web page for details: [https://pages.github.ibm.com/Supply-Chain-Security/AppSec-External-Docs/appsec/CodeSigningService/LocalSign/WhatDoYouWantToSign/artifactswhattosign/](https://pages.github.ibm.com/Supply-Chain-Security/AppSec-External-Docs/appsec/CodeSigningService/LocalSign/WhatDoYouWantToSign/artifactswhattosign/).


