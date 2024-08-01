BEGIN;

ALTER TABLE model_configuration DROP COLUMN IF EXISTS calibration_run_id;
ALTER TABLE semantic DROP CONSTRAINT semantic_type_check;
ALTER TABLE semantic ADD CONSTRAINT semantic_type_check CHECK (type >= 0 AND type <= 3);

COMMIT;
