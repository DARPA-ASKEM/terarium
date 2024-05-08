BEGIN;

ALTER TABLE workflow ALTER COLUMN nodes TYPE json USING nodes::json;
ALTER TABLE workflow ALTER COLUMN edges TYPE json USING edges::json;

COMMIT;
