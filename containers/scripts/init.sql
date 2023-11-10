-- Create Terarium user
CREATE USER terarium_user;
ALTER USER terarium_user WITH PASSWORD 'terarium';

-- Create askem DB for data-service
CREATE DATABASE askem;
GRANT ALL PRIVILEGES ON DATABASE askem TO terarium_user;

-- Create terarium DB for logging
CREATE DATABASE terarium;
GRANT ALL PRIVILEGES ON DATABASE terarium TO terarium_user;

-- Create spice DB for ReBAC
CREATE DATABASE spicedb;
GRANT ALL PRIVILEGES ON DATABASE spicedb TO terarium_user;

-- Create tds for migrating data from TDS to Terarium
CREATE DATABASE tds;
GRANT ALL PRIVILEGES ON DATABASE tds TO terarium_user;
