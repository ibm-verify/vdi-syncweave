
The files in this example are sample response files that are used when doing a silent install.

This example consists of the following files:

TDICustomInstallRsp_Unix.txt     - Response file for doing a custom install, all features selected, on a Unix system
TDICustomInstallRsp_Windows.txt  - Response file for doing a custom install, all features selected, on a Windows system
TDICustomUpgradeRsp_Unix.txt     - Response file for doing an upgrade from IBM Security Verify Directory Integrator 7.1.1 to 7.2 on a Unix system
TDICustomUpgradeRsp_Windows.txt  - Response file for doing an upgrade from IBM Security Verify Directory Integrator 7.1.1 to 7.2 on a Windows system
TDITypicalInstallRsp_Unix.txt    - Response file for doing a typical install on a Unix system
TDITypicalInstallRsp_Windows.txt - Response file for doing a typical install on a Windows system
TDIUninstallRsp_Unix.txt         - Response file for doing an uninstall on a Unix system
TDIUninstallRsp_Windows.txt      - Response file for doing an uninstall on a Windows system


An example on how to use a response file:

Windows:
install_tdiv72_win_x86.exe -f TDITypicalInstallRsp_Windows.txt -i silent 

Unix:
install_tdiv72_linux_x86.bin -f TDITypicalInstallRsp_Unix.txt -i silent 

A response file can be created by invoking an install with the following command line option:

install_tdiv72_win_x86.exe -r customResponseFile.txt

The options and values that are chosen during the install will be recorded into the specified file.  The resulting response file
can be used for silent installs.

