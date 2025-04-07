
# Terarium

A knowledge, modeling and simulation ecosystem.

Welcome to the knowledge-modeling-simulation ecosystem built for researchers.

Empowered with the AI approaches and tools needed for the agile creation, sustainment, an enhancement of complex models and simulators that inform decision-making in diverse missions and scientific domains.

Evaluate and contribute to an accelerating scientific landscape.

[app.terarium.ai](https://app.terarium.ai/)

# ASKEM

This application was built for [DARPA’s Automating Scientific Knowledge Extraction and Modeling (ASKEM)](https://www.darpa.mil/research/programs/automating-scientific-knowledge-extraction-modeling) program to support the demands of complex, modern-day computational models and simulations.

# Application Setup & Usage Guide

### Getting Started
Follow these steps to get Terarium up and running on your local development environment:

#### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd <your-repo-name>/deploy
```

#### 2. Create the Environment File
Inside the `deploy` folder:
1. Copy the environment template file:
```bash
cp .env_template .env
```
2. Open the `.env` file in your preferred editor and add your OpenAI API key where indicated.

#### 3. Set Up hosts File
To ensure the application can resolve the correct hostnames, you may need to add the following line to your `/etc/hosts` file (Linux/Mac) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
```
127.0.0.1  keycloak
127.0.0.1  minio
```

#### 4. Launch the Application
Run the following command from the `deploy` folder:
```bash
docker compose up -d
```
This will start all required system components in the background.

#### 5. Access the Application
Once the containers are running, navigate to: [http://localhost:8080](http://localhost:8080) in your web browser.

### Login Credentials
You can log in using either of the following user accounts:

| Username | Password | Role  |
|----------|----------|-------|
| adam     | admin123 | Admin |
| ursula   | user123  | User  |

### Learn How to Use the Tool
To understand how to use the tool, check out the Help Documentation available within the app.

# License

[Apache License 2.0](LICENSE)
