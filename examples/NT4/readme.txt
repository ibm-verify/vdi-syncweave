
Please note that this demo runs on MS Windows systems (NT/Windows2000) only because it operates with NT/AD security database.

Setup:
This example includes the following configuration files: 
	NT4_iterator.xml,
	NT4_lookup.xml, 
	NT4_addonly.xml,
	NT4_update.xml,
	NT4_delete.xml, 

Properties file: nt.properties, 
Sample output files:
	sample_iterator.xml, 
	sample_lookup.xml.

and readme.txt 

To run this demo you must:
o install SyncWeave.
o have administrator privileges on the NT/AD machine accessed by the NT4 Connector.

There are 5 configuration .xml files included in this package, each demonstrating one NT4 connector mode.
All are using the external property file nt.properties for defining server, username and password for the Connector Configurations.
To run any of the AssemblyLines:
o Edit the file nt.properites to match your local configuration.
o Start the SyncWeave.
o Open the configuration .xml file for the mode you chose (for example, NT4_iterator.xml to run Iterator connector's mode).
o Open AssemblyLine.
o Click the "Run" icon.

You must be aware of the following:
This demo does not alter any existing users and groups in your security database. However, it does add new entries, modifying and deleting them as follows:
o NT4_addonly.xml: creates a new user called "NewGuest".
o NT4_update.xml: updates the "AccountComment" attribute of the user "NewGuest".
o NT4_delete.xml: deletes the user "NewGuest".
To successfully execute the demo and see its results you must always run these AssemblyLines in the following order: 1. NT4_addonly
2. NT4_update
3. NT4_delete


Here is a brief description of what each of the configurations/AssemblyLines does:

o "Iterator" (NT4_iterator.xml): iterates through all users on your NT/AD machine and outputs them to XML in the file <installation_directory>/examples/NT4/iterator.xml.
Before running the AssemblyLine, configure the "Iterator" connector to connect to your machine by setting its "Computer Name" parameter.
The only JavaScript code used in this AssemblyLine can be found in: 
"XMLOutput" connector -> "LogonHours" attribute, and it transforms the original LogonHours attribute (byte[]) into a multiple values attribute in order to be easily displayed by the XML Parser.

Result: 
The <installation_directory>/examples/NT4/iterator.xml file contains all users (and their attributes) that reside on your machine. The sample_iterator.xml file from this package contains the result of this AssemblyLine run on a new installation of Microsoft Windows NT Workstation.


o "Lookup" (NT4_lookup.xml): iterates through all groups on your NT/AD machine, retrieves their names and local/global indicators, looks for their "Comment" attribute and outputs all data retrieved to XML in the file <installation_directory>/examples/NT4/lookup.xml.
Before running the AssemblyLine, configure the "Iterator" and "Lookup" connectors to connect to your machine by setting their "Computer Name" parameters.
Pay attention to the "Link Criteria" in the "Lookup" connector - it links each group with itself retrieving new group's attributes (of course all data can be obtained at once with a single Iterator but this demonstrates the Lookup mode).

Result: 
The <installation_directory>/examples/NT4/lookup.xml file contains all groups (and their attributes) that reside on your machine. The sample_lookup.xml file from this package contains the result of this AssemblyLine run on a new installation of Microsoft Windows NT Workstation.


o "AddOnly" (NT4_addonly.xml): iterates through all users on your NT/AD machine. If a user named "Guest" is found (this is a default NT/AD user), then a new user named "NewGuest" is added with the same attributes as the original "Guest" user.
Before running the AssemblyLine, configure the "Iterator" and "AddOnly" connectors to connect to your machine by setting their "Computer Name" parameters.
JavaScript code is used on the following places:
    (-) "AddOnly" connector -> "Hooks" tab -> "Add Only Loop" -> "Before Add" event: 
Every user with name different from "Guest" is ignored, for example, no add operation is performed for this entry.
    (-) "AddOnly" connector -> "Attribute Map" tab -> "UserName" attribute: 
The name "NewGuest" is set to the new user.
    (-) "AddOnly" Connector -> "Hooks" tab -> "On Error Hook" -> Message logged if entry cannot be added.


Result: 
If user account "NewGuest" does not exist, a new user account "NewGuest" is added in the security database of your NT/AD machine. You can view it by running the Iterator AssemblyLine or with any NT/AD admin tool (for example, "User Manager").


o "Update" (NT4_addonly.xml): If a user named "NewGuest" is found, its "AccountComment" attribute is updated.  The entry is crated in the Script Component CreateEntry.
Before running the AssemblyLine, configure the "Update" connector to connect to your machine by setting its "Computer Name" parameters.
Pay attention to the "Link Criteria" in the "Update" connector - it links each user with itself to achieve the update.
JavaScript code is used on the following places:
	(-) ScriptComponent defines the one and only entry to be processed.
	(-) "Update" Connector -> "Hooks" tab -> "On Error Hook" -> Message logged if entry not found.
	
Result: 
If a user account "NewGuest" exists, its "AccountComment" attribute is updated with the value "A test user account added by SyncWeave". You can view the update by running the Iterator AssemblyLine or with any NT/AD admin tool (for example, "User Manager").


o "Delete" (NT4_delete.xml): iterates through all users on your NT/AD machine. If a user "NewGuest" is found, it is deleted.
Before running the AssemblyLine configure the "Iterator" and "Delete" connectors to connect to your machine by setting their "Computer Name" parameters.
Pay attention to the "Link Criteria" in the "Delete" connector - it links each user with itself (this is how the AssemblyLines know what to delete).
JavaScript code is used in the "Delete" connector ->  "Hooks" tab -> "Delete Loop" -> "Before Delete" event: 
Every user with name different from "NewGuest" is ignored, for example, is not deleted.

Result: 
If a user account "NewGuest" exists, it is deleted from your NT/AD machine. You can view the result by running the Iterator AssemblyLine or with any NT/AD admin tool (for example, "User Manager").

