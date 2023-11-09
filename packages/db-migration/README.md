# Database Migration

Once run, this application will update the database to the latest version (if needed) and then exit.

To migrate the database, create a new file in /resources/db/migration and add your SQL file there. You must
prefix your file with the next version number (e.g. V1_35__what_i_am_changing.sql).

If any V script changes after it has already been applied to a database, the hmi-server will fail on the 
next run since it will fail the checksum check for that migration script.

There is also support to look for data in the resources/db/data directory. All the data scripts are 
repeatable (prefixed with R) which means they will not affect the schema version, and they can be re-run. 
For example, if a script changes (add more data in it) then it will be re-run on the next run of the 
hmi-server. This is in contrast to the regular migration scripts (prefixed with V) which do affect
the schema version and will cause an exception if any of them are modified.
