This example shows how the Command Line Connector can be used.

This example consists of the following files:
cmd.xml, readme.txt

To run this demo you must install IBM Security Verify Directory Integrator.

To run the assembly lines:
1. Start the IBM Security Verify Directory Integrator Config Editor
2. Open the cmd.xml file
3. Select RunOnWindows if you are running on Windows, or select RunOnUnix if you are running on Unix/Linux
4. Click "Run".

Expected result:
You can see the result of the executed command in the AssemblyLine log.

Expected results depend on your environment but in general it is expected that you see lists of environment variables with their values.

Here is a brief description of what each of the components in the cmd.xml file does:
o Parser->DemoScriptParser: is a Parser of type Script Parser, it's modified in order to parse the results of the command.
o Connector->WindowsCommand: Connector of type Command Line. It runs "cmd /c set" command on windows.
o Connector->UnixCommand: Connector of type Command Line. It runs "printenv" command on unix.
o AssemblyLine->RunOnWindows: AssemblyLine using the WindowsCommand Connector and dumping the results to the AssemblyLine log.
o AssemblyLine->RunOnUnix: AssemblyLine using the UnixCommand Connector and dumping the results to the AssemblyLine log.

Important notes:
o For your special programs that you can run you might need special Parsers. First view the list of Parsers. If you don't find one that you can use, then you need to write your own Parser. Please refer to online documentation for this.

o If you run on I5/OS, you should go to the Connectors Folder, choose the UnixCommand Connector,
select Parser tab -> Advanced, and give the correct character encoding in the Character Encoding
field, e.g. IBM037. You can then choose RunOnUnix as the AssemblyLine to run.
