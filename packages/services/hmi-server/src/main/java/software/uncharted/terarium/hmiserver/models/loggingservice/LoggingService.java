package software.uncharted.terarium.hmiserver.models.loggingservice;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;

@ApplicationScoped
@Slf4j
@Data
@Accessors(chain = true)
public class LoggingService implements Serializable {
    private List<JsonObject> logs;

    public void echoLogs(String name){
        this.logs.forEach(l -> logMessage(l, name));
    }

    private void logMessage(JsonObject payload, String name) {
        String level = payload.getString("level");
        String message = "HMI-Log | " + name + " | " + payload.getString("message");

        switch (level) {
            case "trace":
                log.trace(message);
                break;
            case "debug":
                log.debug(message);
                break;
            case "info":
                log.info(message);
                break;
            case "warn":
                log.warn(message);
                break;
            case "error":
                log.error(message);
                break;
            default:
                log.info("Invalid logging level, defaulting to info level: " + message);
                break;
        }
    }

}