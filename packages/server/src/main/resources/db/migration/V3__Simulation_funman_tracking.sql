BEGIN;
ALTER TABLE simulation DROP CONSTRAINT simulation_type_check;
ALTER TABLE simulation ADD CONSTRAINT simulation_type_check CHECK(type = ANY(ARRAY['ENSEMBLE', 'SIMULATION', 'CALIBRATION', 'VALIDATION']));

ALTER TABLE simulation ALTER COLUMN execution_payload TYPE text;
ALTER TABLE simulation ALTER COLUMN description TYPE VARCHAR(1000);
ALTER TABLE simulation ALTER COLUMN result_files TYPE VARCHAR(1000)[];
ALTER TABLE simulation ALTER COLUMN status_message TYPE text;
COMMIT;
