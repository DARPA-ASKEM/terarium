package software.uncharted.terarium.esingest.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.input.IInputEmbeddingChunk;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputEmbeddingChunk;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticDocumentIngestService extends ConcurrentWorkerService {

	private final ElasticsearchService esService;
	private final ElasticsearchConfiguration esConfig;

	/**
	 * Ingests document data from source directory into elasticsearch.
	 *
	 * Iterates over each file in the source directory. Each line is a single
	 * document. Lines are sent to workers to be processed and ingested.
	 *
	 * @param params - The ingest parameters.
	 * @param ingest - The ingest class implementation.
	 *
	 * @return - A list of errors encountered during ingest.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<String> ingestData(
			ElasticIngestParams params,
			IElasticIngest<IInputDocument, IOutputDocument, IInputEmbeddingChunk, IOutputEmbeddingChunk> ingest)
			throws IOException, InterruptedException, ExecutionException {

		List<String> errors = Collections.synchronizedList(new ArrayList<>());
		BlockingQueue<List<String>> workQueue = new LinkedBlockingQueue<>(params.getWorkQueueSize());

		AtomicLong lastTookMs = new AtomicLong(0);

		startWorkers(workQueue, (List<String> items, Long timeWaitingOnQueue) -> {
			try {
				long start = System.currentTimeMillis();
				List<IOutputDocument> output = new ArrayList<>();
				for (String item : items) {
					IInputDocument input = ingest.deserializeDocument(item);
					IOutputDocument out = ingest.processDocument(input);
					if (out != null) {
						output.add(out);
					}
				}

				long sinceLastTook = (System.currentTimeMillis() - start) + timeWaitingOnQueue;
				long backpressureWait = lastTookMs.get() - sinceLastTook;
				if (backpressureWait > 0) {
					// apply backpressure
					Thread.sleep(backpressureWait);
				}

				ElasticsearchService.BulkOpResponse res = esService
						.bulkIndex(esConfig.getIndex(params.getOutputIndexRoot()), output);
				if (res.getErrors().size() > 0) {
					errors.addAll(res.getErrors());
					if (errors.size() > params.getErrorsThreshold()) {
						for (String err : errors) {
							log.error(err);
						}
						throw new InterruptedException("Too many errors, stopping ingest");
					}
				}
				lastTookMs.set(res.getTook());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		readLinesIntoQueue(workQueue, params.getDocumentBatchSize(),
				Paths.get(params.getInputDir()).resolve("documents"));

		waitUntilWorkersAreDone(workQueue);

		return errors;
	}

}
