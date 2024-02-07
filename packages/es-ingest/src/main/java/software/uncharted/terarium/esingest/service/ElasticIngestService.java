package software.uncharted.terarium.esingest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.util.TimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticIngestService extends ConcurrentWorkerService {

	private final ElasticInsertService esInsertService;

	private final ElasticUpdateService esUpdateService;

	private final ElasticsearchService esService;

	private final ElasticsearchConfiguration esConfig;

	public void ingest(ElasticIngestParams params, IElasticIngest ingest)
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

			// setup ingest
			ingest.setup(params);

			Queue<IElasticPass<? extends IInputDocument, ? extends IOutputDocument>> passes = new LinkedList<>(
					ingest.getPasses());

			if (passes.isEmpty()) {
				throw new RuntimeException("No passes found in ingest");
			}

			long start = System.currentTimeMillis();
			List<String> errs = new ArrayList<>();

			IElasticPass<? extends IInputDocument, ? extends IOutputDocument> insertPass = passes.poll();

			log.info("Ingesting documents from {} into {}", params.getInputDir(), indexName);

			long insertStart = System.currentTimeMillis();

			insertPass.setup(params);
			errs.addAll(esInsertService.insertDocuments(params, insertPass));
			insertPass.teardown(params);
			esInsertService.shutdown();

			log.info("Ingested documents successfully in {}",
					TimeFormatter.format(System.currentTimeMillis() - insertStart));

			for (IElasticPass<? extends IInputDocument, ? extends IOutputDocument> updatePass : passes) {
				long updateStart = System.currentTimeMillis();
				updatePass.setup(params);
				errs.addAll(esUpdateService.updateDocuments(params, updatePass));
				updatePass.teardown(params);
				log.info("Updated documents successfully in {}",
						TimeFormatter.format(System.currentTimeMillis() - updateStart));
			}
			esUpdateService.shutdown();

			// teardown ingest
			ingest.setup(params);

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
