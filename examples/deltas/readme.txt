# Delta Processing Example

This example demonstrates how Delta processing detects and applies changes between source data sets and a target database.

## Files

- deltas.xml - AssemblyLine configuration
- users1.txt - Initial source data
- users2.txt - Updated source data containing adds, changes, and deletions

## Prerequisites

- A supported JDBC database (Derby, etc.)
- JDBC Connectors configured with the appropriate driver, URL, and credentials

## How It Works

### Initial Load

The AssemblyLine reads users1.txt and:

- Inserts all records into the target database
- Creates a Delta Store
- Stores source records for future comparisons

### Delta Synchronization

The AssemblyLine then processes users2.txt and compares it against the Delta Store.

Records are classified as:

- Add - New record
- Modify - Existing record changed
- Delete - Record removed from source
- Unchanged - No action required

Synchronization is performed using three JDBC Connectors:

- DB_INSERT (AddOnly)
- DB_UPDATE (Update)
- DB_DELETE (Delete)

Only changed records are processed.

## Running the Example

### First Run

1. Start SyncWeave Admin.
2. Open deltas.xml.
3. Configure the ReadTextFile connector to use:

   users1.txt

4. Run the AssemblyLine.

Result:

- Database is populated with records from users1.txt.
- Delta Store is created and initialized.

### Second Run

1. Change the ReadTextFile connector input file to:

   users2.txt

2. Run the AssemblyLine again.

Result:

- New records are inserted.
- Modified records are updated.
- Deleted records are removed.
- Unchanged records are skipped.

The target database is synchronized to match the contents of users2.txt.

## Custom Logic

JavaScript hooks are used by the JDBC Connectors during insert, update, and delete processing to apply the required database operations.
