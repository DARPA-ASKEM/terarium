package software.uncharted.terarium.esingest.configuration;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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
