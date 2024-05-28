BEGIN;

ALTER TABLE notification_event ALTER COLUMN data TYPE json USING data::json;

COMMIT;
