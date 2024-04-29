BEGIN;
ALTER TABLE simulation ALTER COLUMN result_files TYPE VARCHAR(1024)[];
ALTER TABLE simulation ALTER COLUMN description TYPE text;
ALTER TABLE workflow ALTER COLUMN description TYPE text;
ALTER TABLE project ALTER COLUMN description TYPE text;
COMMIT;
