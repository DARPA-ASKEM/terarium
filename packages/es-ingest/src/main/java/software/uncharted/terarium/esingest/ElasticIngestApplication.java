package software.uncharted.terarium.esingest;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import software.uncharted.terarium.esingest.configuration.Config;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;
import software.uncharted.terarium.esingest.service.ElasticIngestService;

@SpringBootApplication
@Slf4j
@PropertySource("classpath:application.properties")
public class ElasticIngestApplication {

  @Autowired ElasticIngestService esIngestService;

  @Autowired Config config;

  public static void main(String[] args) {
    SpringApplication.run(ElasticIngestApplication.class, args);
  }

  @Bean
  public ApplicationRunner applicationRunner() {
    return args -> {
      if (config.getIngestParams().size() == 0) {
        log.error("No ingest parameters configured. Exiting...");
        System.exit(1);
      }

      List<IElasticIngest> ingests = new ArrayList<>();

      for (ElasticIngestParams params : config.getIngestParams()) {
        log.info("Loading ingest class: {}", params.getIngestClass());

        Class<?> ingestClass = Class.forName(params.getIngestClass());
        Constructor<?> constructor = ingestClass.getConstructor();
        IElasticIngest ingest = (IElasticIngest) constructor.newInstance();
        ingests.add(ingest);
      }

      for (int i = 0; i < config.getIngestParams().size(); i++) {

        ElasticIngestParams params = config.getIngestParams().get(i);
        IElasticIngest ingest = ingests.get(i);

        esIngestService.ingest(params, ingest);
      }

      esIngestService.shutdown();

      log.info("Shutting down the application...");
      System.exit(0);
    };
  }
}
