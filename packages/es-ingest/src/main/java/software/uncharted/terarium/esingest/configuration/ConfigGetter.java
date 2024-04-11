package software.uncharted.terarium.esingest.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigGetter {

  private static Config config;

  public ConfigGetter(Config c) {
    ConfigGetter.config = c;
  }

  public static Config getConfig() {
    return config;
  }
}
