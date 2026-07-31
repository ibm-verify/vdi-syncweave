This example demonstrates the use of the SNMPConnector in SyncWeave.


This package contains 3 files:  
readme.txt, snmp.xml and expected_snmpTrap.txt.

snmp.xml contains two AssemblyLines:  
sendTrap and grabTrap

sendTrap waits for some seconds, before sending 5 traps to port 5488. 
grabTrap listens to port 5488 and outputs a short message to the file snmpTrap.txt for each received trap. It terminates after 5 iterations (set in config panel of AL).  

1. Start the SyncWeave Config Editor.
2. Open examples/snmpTrap/snmp.xml.
3. Select the AssemblyLine called grabTrap. Click "Start".
4. Select the AssemblyLine called sendTrap. Click "Start".

When the two AssemblyLines have finished, you have a file called snmpTrap.txt with the same content as expected_snmpTrap in it.

Relevant code is found:

sendTrap contains all code in Before Connectors Initialized.  Note the port number (5488) that must match with the port listened to in grabTrap.
grabTrap does a dummy attribute mapping of the attribute message returned by the SNMP Connector called SNMP.  This attribute is later ignored. The Connector ACK simply writes a line to the outputfile.

