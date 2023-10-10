-- Create askem DB
CREATE USER terarium_user;
ALTER USER terarium_user WITH PASSWORD 'terarium';
CREATE DATABASE askem;
GRANT ALL PRIVILEGES ON DATABASE askem TO terarium_user;

-- Create terarium DB
CREATE DATABASE terarium;
GRANT ALL PRIVILEGES ON DATABASE terarium TO terarium_user;
