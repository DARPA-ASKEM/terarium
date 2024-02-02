package software.uncharted.terarium.esingest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.esingest.models.input.covid.CovidDocument;
import software.uncharted.terarium.esingest.models.input.covid.CovidEmbedding;
import software.uncharted.terarium.esingest.models.output.Document;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.EmbeddingChunk;
import software.uncharted.terarium.esingest.service.ElasticDocumentIngestService;
import software.uncharted.terarium.esingest.service.ElasticEmbeddingIngestService;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;
import software.uncharted.terarium.esingest.service.ElasticsearchService;
import software.uncharted.terarium.esingest.util.TimeFormatter;

@SpringBootApplication
@Slf4j
@PropertySource("classpath:application.properties")
public class ElasticIngestApplication {

	@Autowired
	ElasticsearchConfiguration esConfig;

	@Autowired
	ElasticDocumentIngestService esDocumentIngestService;

	@Autowired
	ElasticEmbeddingIngestService esEmbeddingIngestService;

	@Autowired
	ElasticsearchService esService;

	@Autowired
	ApplicationContext context;

	@Value("${terarium.esingest.input-dir}")
	String inputDir;

	@Value("${terarium.esingest.output-index}")
	String outputIndex;

	public static void main(String[] args) {
		SpringApplication.run(ElasticIngestApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			try {
				ElasticIngestParams params = new ElasticIngestParams();
				params.setInputDir(inputDir);
				params.setOutputIndex(outputIndex);

				// ensure the index is empty
				esService.createOrEnsureIndexIsEmpty(outputIndex);

				long start = System.currentTimeMillis();

				long documentStart = System.currentTimeMillis();
				log.info("Ingesting documents");

				List<String> errs = new ArrayList<>();
				errs.addAll(esDocumentIngestService.ingestData(params,
						(CovidDocument input) -> {

							Document<Embedding> doc = new Document<>();
							doc.setId(input.getId());
							doc.setTitle(input.getSource().getTitle());
							doc.setFullText(input.getSource().getBody());

							return doc;
						}, CovidDocument.class));

				esDocumentIngestService.shutdown();

				log.info("Ingested documents successfully in {}",
						TimeFormatter.format(System.currentTimeMillis() - documentStart));

				long embeddingStart = System.currentTimeMillis();
				log.info("Ingesting embeddings");

				errs.addAll(esEmbeddingIngestService.ingestData(params,
						(CovidEmbedding input) -> {

							Embedding embedding = new Embedding();
							embedding.setEmbeddingId(input.getEmbeddingChunkId());
							embedding.setSpans(input.getSpans());
							embedding.setVector(input.getEmbedding());

							EmbeddingChunk<Embedding> chunk = new EmbeddingChunk<>();
							chunk.setId(input.getId());
							chunk.setEmbedding(embedding);

							return chunk;

						}, CovidEmbedding.class));

				esEmbeddingIngestService.shutdown();

				log.info("Ingested embeddings successfully in {}",
						TimeFormatter.format(System.currentTimeMillis() - embeddingStart));

				log.info(
						"Ingest completed successfully in {}",
						TimeFormatter.format(System.currentTimeMillis() - start));
				for (String err : errs) {
					log.error(err);
				}

				log.info("Shutting down the application gracefully...");
				// Shut down the application gracefully
				System.exit(0);
			} catch (Exception e) {
				log.info("Ingest failed");
				e.printStackTrace();

				log.info("Shutting down the application gracefully...");
				System.exit(1);
			}
		};
	}
}
