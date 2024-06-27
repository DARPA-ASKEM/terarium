package software.uncharted.terarium.ingest;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.ingest.configuration.Config;
import software.uncharted.terarium.ingest.ingests.IIngest;
import software.uncharted.terarium.ingest.service.DocumentIngestService;
import software.uncharted.terarium.ingest.service.IngestParams;

@SpringBootApplication
@Slf4j
@PropertySource("classpath:application.properties")
public class IngestApplication {

	@Autowired
	DocumentIngestService documentIngestService;

	@Autowired
	Config config;

	public static void main(final String[] args) {
		SpringApplication.run(IngestApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {

			if (config.getIngestParams().size() == 0) {
				log.error("No ingest parameters configured. Exiting...");
				System.exit(1);
			}

			final List<IIngest> ingests = new ArrayList<>();

			for (final IngestParams params : config.getIngestParams()) {
				log.info("Loading ingest class: {}", params.getIngestClass());

				final Class<?> ingestClass = Class.forName(params.getIngestClass());
				final Constructor<?> constructor = ingestClass.getConstructor();
				final IIngest ingest = (IIngest) constructor.newInstance();
				ingests.add(ingest);
			}

			for (int i = 0; i < config.getIngestParams().size(); i++) {

				final IngestParams params = config.getIngestParams().get(i);
				final IIngest ingest = ingests.get(i);

				switch (params.getAssetType()) {
					case DOCUMENT:
						documentIngestService.ingest(params, ingest);
						break;
					default:
						throw new RuntimeException("Unsupported asset type");
				}
			}

			log.info("Shutting down the application...");
			System.exit(0);
		};
	}
}
