// NTService.cpp
// 
// This is the main program file containing the entry point.

#include "NTServApp.h"
#include "IBMDIService.h"

int main(int argc, char* argv[])
{
    // Create the service object
    CIBMDIService service;
    
    // Parse for standard arguments (install, uninstall, version etc.)
    if (!service.ParseStandardArgs(argc, argv)) {

        // Didn't find any standard args so start the service
        // Uncomment the DebugBreak line below to enter the debugger
        // when the service is started.
        //DebugBreak();
        service.StartService();
    }

    // When we get here, the service has been stopped
    return service.m_Status.dwWin32ExitCode;
}
