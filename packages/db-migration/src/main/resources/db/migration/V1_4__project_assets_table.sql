CREATE TABLE project_asset (
	id 					  SERIAL                   NOT NULL,
	project_id    INTEGER                  NOT NULL,
	resource_id   CHARACTER VARYING        NOT NULL,
	resource_type resource_type            NOT NULL,
	external_ref  CHARACTER VARYING,
	created_on    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	updated_on    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deleted_on    TIMESTAMP WITH TIME ZONE
);

CREATE TRIGGER project_asset_updated
	BEFORE UPDATE
	ON project_asset
	FOR EACH ROW EXECUTE PROCEDURE updated();
