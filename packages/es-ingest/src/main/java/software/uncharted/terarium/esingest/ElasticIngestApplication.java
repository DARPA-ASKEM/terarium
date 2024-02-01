package software.uncharted.terarium.esingest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import software.uncharted.terarium.esingest.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.esingest.models.input.covid.CovidDocument;
import software.uncharted.terarium.esingest.models.input.covid.CovidEmbedding;
import software.uncharted.terarium.esingest.models.output.Document;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;
import software.uncharted.terarium.esingest.service.ElasticIngestService;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ElasticIngestApplication {

	@Autowired
	ElasticsearchConfiguration esConfig;

	@Autowired
	ElasticIngestService esIngestService;

	@Autowired
	ApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(ElasticIngestApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			try {
				ElasticIngestParams params = new ElasticIngestParams();
				params.setInputDir("/home/kbirk/Downloads/covid");
				params.setOutputIndex(esConfig.getCovidIndex());

				esIngestService.ingestData(params,
						(CovidDocument input) -> {

							Document doc = new Document();
							doc.setId(input.getId());
							doc.setTitle(input.getTitle());
							doc.setFullText(input.getBody());

							return doc;
						},
						(CovidEmbedding input) -> {

							Embedding embedding = new Embedding();
							embedding.setDocumentId(input.getDocumentId());
							embedding.setEmbeddingChunkId(input.getEmbeddingChunkId());

							// TODO: fix this
							embedding.setTitle(null);
							embedding.setSpans(null);
							embedding.setVector(null);

							return embedding;
						});

				// Shut down the application gracefully
				SpringApplication.exit(context, () -> 0);
			} catch (Exception e) {
				e.printStackTrace();

				SpringApplication.exit(context, () -> 1);
			}

		};
	}
}
