-- add new OPTIMIZATION type to simulation
BEGIN;
ALTER TABLE simulation
	DROP CONSTRAINT IF EXISTS simulation_type_check;
ALTER TABLE simulation
	ADD CONSTRAINT simulation_type_check CHECK (type::text = ANY (ARRAY['ENSEMBLE'::text, 'SIMULATION'::text, 'CALIBRATION'::text, 'VALIDATION'::text, 'OPTIMIZATION'::text]));
COMMIT;
