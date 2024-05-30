-- Create Terarium user
CREATE USER terarium_user;
ALTER USER terarium_user WITH PASSWORD 'terarium';

-- Create terarium DB for logging
CREATE DATABASE terarium;
GRANT ALL PRIVILEGES ON DATABASE terarium TO terarium_user;

-- Create spice DB for ReBAC
CREATE DATABASE spicedb;
GRANT ALL PRIVILEGES ON DATABASE spicedb TO terarium_user;
