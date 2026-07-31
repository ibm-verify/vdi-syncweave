
This is a simple SyncWeave configuration that demonstrates the SyncWeave's Domino Users Connector.

NOTE: SyncWeave must be installed on the same machine as the Domino server, i.e. locally. Even though the Domino Users Connector supports both local and remote deployment, this example covers the local deployment only.

This example consists of the following files:
dominoconnector.xml, addreg.csv, updatereg.csv, readme.txt

Following are the requirements while running Domino Users Connector.

1. Domino server is running on the same system as that of IDI.
2. All the Configuration and Security settings are done as per IBM Directory
   Integrator reference guide.

Following are the assumptions while running Domino Users Connector.

1. The defaults path for Domino Installation are /opt/lotus/bin and 
   /local/notesdata. If the paths used for domino installation are different 
   from these, please modify the input files accordingly.
2. Create a directory called UserData under /local/notesdata.
3. All the passwords for admin, certifier and the user passwords are kept as 
   "password" for simplicity. Please modify them as needed.


This demo consists of four AssemblyLines.  Each does one task:
"AddNReg" -- reads user information from a comma separated file and adds (and registers) the
user to the Domino server

"IterateALL" -- connects to the Domino server's address book (names.nsf) and dumps all data to a file (IterateALL_out.txt)

"UpdateRec" -- reads some users from a file and updates their information in Domino

"DeleteRec" -- connects to the Domino server and deletes the entries for all users whose last name = "idiuser2" (look at the link criteria for the DomDelete connector).


To run this demo:
1. start the SyncWeave Admin.
2. Open the dominoconnector.xml file.
3. Open the AssemblyLine you wish to run.
4. Change the passwords on the Domino Connector's to match those of your server.
5. Click the "run" button to execute the assembly line.
