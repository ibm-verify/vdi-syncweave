This is a simple SyncWeave configuration that demonstrates the SyncWeave's Remote Command 
Line Function Component (Remote CLFC).

This example consists of the following files:
remoteCLFC.xml, readme.txt

The following are the requirements while running the Remote Command Line Function Component.

1. The local machine must have SyncWeave installed.
2. The machine you wish to execute the remote command on, (the target), must be available on the network from the local machine.
That is, you should be able to successfully ping the target machine by hostname from your SyncWeave machine.
3. The target machine must have one of the available protocols (SSH, RSH, REXEC or Windows) installed and configured and running 
in order for the Remote CLFC to successfully connect and perform a command.

All the Configuration and Security settings should be done as per IBM Directory Integrator reference guide.

This demo consists a single AssemblyLine described below:
"ProcessStdOutput" -- Performs a directory listing on the target machine. The Assembly line contains an instance of the Remote CLFC that is 
configured to use any protocol to connect to the target and it has been pre-configured with the 'ls' command.
This component is followed by a Script component that contains script demonstrating how to access the attribute containing the standard output from 
the configured command. The script then uses a Java BufferedReader to demonstrate how the returned output can be processed line-by-line.

To run this demo:

1. Start the IBM Directory Integrator Config Editor.
2. Open the remoteCLFC.xml file included in this example.
3. Go to the "AssemblyLines" section.
4. Select the "processStdOutput" AssemblyLine.
5. Select the "remCLFC" Function Component from under 'Data Flow'.
6. Fill in the hostname of the target machine and sufficient authentication details to connect to the remote machine.
That is, a remote user name as well as a password or a valid keystore/passphrase combination.
NOTE: You may modify the "Command" value to perform a command other than 'ls' on the target machine.  
You can also select the appropriate connection protocol if you wish.
7. Click the "run" button to execute the assembly line.
