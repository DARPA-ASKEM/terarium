BEGIN;

ALTER TABLE simulation ALTER COLUMN execution_payload TYPE json USING execution_payload::json;
ALTER TABLE simulation_update ALTER COLUMN data TYPE json USING data::json;

COMMIT;
