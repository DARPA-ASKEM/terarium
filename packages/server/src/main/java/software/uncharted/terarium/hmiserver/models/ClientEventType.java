package software.uncharted.terarium.hmiserver.models;

public enum ClientEventType {
    HEARTBEAT,
    NOTIFICATION,
    SIMULATION_SCIML,
    SIMULATION_PYCIEMSS,
    FILE_UPLOAD_PROGRESS,
    EXTRACTION,
    EXTRACTION_PDF,
}
