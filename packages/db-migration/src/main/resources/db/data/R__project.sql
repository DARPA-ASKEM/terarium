INSERT INTO project
    (id, name, description, created_on, updated_on, deleted_on)
VALUES
    (1, 'ASKEM Epi Demo Project', 'ASKEM Epi Demo Project', '2023-01-01 00:00:00', '2023-01-01 00:00:00', NULL)
ON CONFLICT DO NOTHING;
INSERT INTO project
    (id, name, description, created_on, updated_on, deleted_on)
VALUES
    (2, 'ASKEM Climate Demo Project', 'ASKEM Climate Demo Project', '2023-01-01 00:00:00', '2023-01-01 00:00:00', NULL)
ON CONFLICT DO NOTHING;
