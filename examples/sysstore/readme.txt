System Store Connector Example.
======================

Overview:
This example shows how to use the System Store Connector to write entries to the System Store and then read everything back in on a sorted key to sort their data. 

How to run the example:
1. Start IBM Security Verify Directory Integrator CE.
2. Create a new CE project ('File'->'New'->'Project').
3. Import the example configuration in your project (by right clicking on it and choosing 'Import...').
3.1. In the opened dialog specify 'IBM Security Verify Directory Integrator' and 'Configuration'. 
3.2. In the next dialog browse to the location of the configuration xml (<install_dir>/examples/sysstore/SysStoreExample.xml)
and click 'Finish'. 
4. Go to the "AssemblyLines" section.
5. Start the "put" AssemblyLine by clicking 'Run'.
6. Then start AssemblyLine "get".

Brief explanation of the example�s workflow:

AssemblyLines:

"put"   - this AssemblyLine contains File System and System Store connectors. The File System connector 
          reads entries from in.txt file using Simple parser. All attributes of the read entries are
          mapped plus one additional attribute used for sorting - sortStr. It is formed by 
          concatenating the values of the attributes we want to use for sorting - in this case name and
          id. The System Store connector writes the read entries to the underlying System Store by using
          the sortStr attribute for Key Attribute name (used for primary key in the created table).


"get"   - this AssemblyLine contains System Store and File System connectors. The System Store connector
          connects to the created by the first AssemblyLine table and reads entries in a sorted way.
          This is acheived by specifying System Store connector's SQL Select statement to 
                'SELECT * FROM PEOPLE ORDER BY ID'
          The ID column contains the sortStr string previously created in AssemblyLine "put".
          The "Delete table on close" param is set to allow this example to be run multiple times
          without errors (caused by duplicate primary keys).
          
          The File System connector writes the read entries into out.txt file using Simple parser. The 
          result in this file should contain all entries from the in.txt file, but sorted first by name
          and then by id.