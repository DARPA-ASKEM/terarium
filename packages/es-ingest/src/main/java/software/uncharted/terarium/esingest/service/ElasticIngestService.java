package software.uncharted.terarium.esingest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.input.IInputEmbeddingChunk;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputEmbeddingChunk;
import software.uncharted.terarium.esingest.util.TimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticIngestService extends ConcurrentWorkerService {

	private final ElasticDocumentIngestService esDocumentIngestService;

	private final ElasticEmbeddingIngestService esEmbeddingIngestService;

	private final ElasticsearchService esService;

	private final ElasticsearchConfiguration esConfig;

	public void ingest(ElasticIngestParams params,
			IElasticIngest<IInputDocument, IOutputDocument, IInputEmbeddingChunk, IOutputEmbeddingChunk> ingest)
			throws IOException, InterruptedException, ExecutionException {

		log.info("Running ingest: {}", params.getName());
		String indexName = esConfig.getIndex(params.getOutputIndexRoot());

		try {
			// ensure the index is empty
			if (params.isClearBeforeIngest()) {
				if (esService.containsIndex(indexName)) {
					esService.deleteIndex(indexName);
				}
				esService.createIndex(indexName);
			} else {
				if (!esService.containsIndex(indexName)) {
					esService.createIndex(indexName);
				}
			}
			esService.createOrEnsureIndexIsEmpty(indexName);

			long start = System.currentTimeMillis();

			long documentStart = System.currentTimeMillis();

			log.info("Ingesting documents from {} into {}", params.getInputDir(), indexName);

			List<String> errs = new ArrayList<>();
			errs.addAll(esDocumentIngestService.ingestData(params, ingest));

			esDocumentIngestService.shutdown();

			log.info("Ingested documents successfully in {}",
					TimeFormatter.format(System.currentTimeMillis() - documentStart));

			long embeddingStart = System.currentTimeMillis();

			log.info("Ingesting embeddings");
			errs.addAll(esEmbeddingIngestService.ingestData(params, ingest));

			esEmbeddingIngestService.shutdown();

			log.info("Ingested embeddings successfully in {}",
					TimeFormatter.format(System.currentTimeMillis() - embeddingStart));

			log.info(
					"Ingest completed successfully in {}",
					TimeFormatter.format(System.currentTimeMillis() - start));

			if (errs.size() > 0) {
				log.warn("Ingest encountered {} errors:", errs.size());
				for (String err : errs) {
					log.error(err);

				}
			}

		} catch (Exception e) {
			log.error("Ingest failed", e);
		}
	}
}
