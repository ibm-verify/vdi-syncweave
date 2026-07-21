This example demonstrates how to call a DB2 Stored Procedure using JDBC Connector and return the results from stored procedure call to IBM Security Verify Directory Integrator.


Explanation:
1) The information center contains a sample of a stored procedure call from IBM Security Verify Directory Integrator, but the example returns a true or false based on the successful execution.
2) The following example returns values from the stored procedure call. 
3) The sample can be used with any version of IBM Security Verify Directory Integrator and/or DB2.

This example consists of the following files:
Storedprocedure.xml Readme.txt

ENVIRONMENT: TDI 7.* and DB2 9.*

1) CREATE STORED PROCEDURE USING DB2

db2 =>

    CREATE PROCEDURE TESTMULTIRS2 (IN i_cmacct CHARACTER(5)) RESULT SETS 2 LANGUAGE SQL BEGIN DECLARE csnum INTEGER;
    DECLARE getDeptNo CHAR(50); DECLARE getDeptName CHAR(20);
    DECLARE c1 CURSOR WITH RETURN FOR s1;
    DECLARE c2 CURSOR WITH RETURN FOR s2;
    SET getDeptNo = 'SELECT DEPTNO FROM DEPT';
    SET getDeptName = 'SELECT DEPTNAME FROM DEPT';
    PREPARE s1 FROM getDeptNo; OPEN c1;
    PREPARE s2 FROM getDeptName; OPEN c2;
    END

DB20000I The SQL command completed successfully.
db2 =>

2) EXECUTE STORED PROCEDURE CALL USING DB2 COMMAND

To execute the procedure in DB2:

    C:\Program Files\IBM\SQLLIB\BIN>db2
        db2 => connect to testtdi
        Database Connection Information
        Database server = DB2/NT 9.1.3
        SQL authorization ID = ADMINIST...
        Local database alias = TESTTDI

        db2 => call testmultirs('arc')
        Result set 1
            DEPTNO
            U2LA 1 record(s) selected.
        Result set 2
            DEPTNAME
            IBM TDI L2 Support 1 record(s) selected.
        Return Status = 0
        db2 => 


3) TDI Configuration 
   1) Create a JDBC connector in Iterator mode passive state. 
		
		SAMPLE JDBC CONNECTOR CONFIGURATION

		Section: General
		jdbcSource = jdbc:db2://<mydb2hosname.com>:<50000>/<testtdi>
		jdbcDriver = com.ibm.db2.jcc.DB2Driver
		jdbcLogin = <db2username>
		jdbcPassword = <db2password>
		jdbcSchema = null
		jdbcTable = DEPT
		
		Replace <> data with your setup information
		
	2) Create a sample script component with the sample code given below.
	
		SAMPLE CODE FOR A SCRIPT COMPONENT
		
		var con = jdbcTovTulip.connector.connection;
		// java.sql.Connection where jdbcTovTulip is the Connector component name
		var cstmt // java.sql.CallableStatement
		var resultSet // java.sql.ResultSet;

		try {
		// PreparedStatement call
			cstmt = con.prepareCall("{call TESTMULTIRS(?)}");
			cstmt.setString(1, 'arc');

			success = cstmt.execute();
			task.logmsg("Results from execute >> " + success);
			if (!success) return 0;
			var more = true;
			var num = 0;
			while (more) {
			num++;
			task.logmsg("ResultSet " + num)
			resultSet = cstmt.getResultSet();
			metaData = resultSet.getMetaData()
			var columns = metaData.getColumnCount();
			while (resultSet.next()) 
			{
				for (i=1; i <= columns; i++) 
				{ 
					task.logmsg("Column " + i + " Name >> " + metaData.getColumnName(i));
					task.logmsg("Value >> " + resultSet.getString(i)); 
				}
			}
			more = cstmt.getMoreResults(); }
		}
		catch(e) { 
		task.logmsg("Exception message: " + e); 
		}


	3) RESULTS AFTER RUNNING IN CONSOLE
		Results from execute >> true
		ResultSet 1
		Column 1 Name >> DEPTNO

            Value >> U2LA 

		ResultSet 2
		Column 1 Name >> DEPTNAME

            Value >> IBM TDI L2 Support

This demo provides one AssemblyLine that has the above configuration.

To run "ConnectToDB2":
1. Start the IBM Security Verify Directory Integrator Config Editor.
2. Import the StoredProcedure.xml file.
3. Open the "AssemblyLine" branch.
4. Select "ConnectToDB2" AssemblyLine.
5. Click "Run."
6. Check the generated output as shown below.
	Results from execute >> true
		ResultSet 1
		Column 1 Name >> DEPTNO

            Value >> U2LA 

		ResultSet 2
		Column 1 Name >> DEPTNAME

            Value >> IBM TDI L2 Support

o For more detailed information on the relevant topic refer to the online documentation.
