BEGIN;

ALTER TABLE project_asset
  DROP CONSTRAINT project_asset_asset_type_check;

ALTER TABLE project_asset
  ADD CONSTRAINT project_asset_asset_type_check
  CHECK (asset_type::text = ANY (ARRAY[
    'WORKFLOW'::character varying,
    'MODEL'::character varying,
    'DATASET'::character varying,
    'SIMULATION'::character varying,
    'DOCUMENT'::character varying,
    'CODE'::character varying,
    'MODEL_CONFIGURATION'::character varying,
    'ARTIFACT'::character varying,
    'INTERVENTION_POLICY'::character varying,
    'NOTEBOOK_SESSION'::character varying
  ]));

COMMIT;
