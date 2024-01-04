CREATE TABLE project
(
    id          SERIAL                   PRIMARY KEY,
    name        TEXT                     NOT NULL,
    description TEXT,
    created_on  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_on  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deleted_on  TIMESTAMP WITH TIME ZONE
);

CREATE TRIGGER projects_updated
    BEFORE UPDATE
    ON project
    FOR EACH ROW EXECUTE PROCEDURE updated();
