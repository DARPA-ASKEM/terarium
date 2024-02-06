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
public class ElasticEmbeddingIngestService extends ConcurrentWorkerService {

	private final ElasticsearchService esService;
	private final ElasticsearchConfiguration esConfig;

	/**
	 * Ingests embedding data from source directory into elasticsearch.
	 *
	 * Iterates over each file in the source directory. Each line is an "embedding
	 * chunk", a single document will have 1 to N embedding chunks.
	 * Chunks are sent to workers to be built into arrays and added to each
	 * respective parent document.
	 *
	 * @param params - Params relating to the ingest.
	 * @param ingest - The ingest class implementation.
	 *
	 * @return - A list of errors encountered during the ingest.
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

		BlockingQueue<List<IInputEmbeddingChunk>> workQueue = new LinkedBlockingQueue<>(params.getWorkQueueSize());

		AtomicLong lastTookMs = new AtomicLong(0);
		startWorkers(workQueue, (List<IInputEmbeddingChunk> items, Long timeWaitingOnQueue) -> {
			try {
				long start = System.currentTimeMillis();

				List<IOutputDocument> output = new ArrayList<>();

				IOutputDocument partial = null;
				for (IInputEmbeddingChunk item : items) {
					IOutputEmbeddingChunk out = ingest.processEmbedding(item);
					if (out != null) {
						if (partial == null) {
							// create a new partial
							partial = out.createPartial();
						} else if (!partial.getId().equals(item.getId())) {
							// embedding references a new doc, add existing partial to output, create next
							// one
							output.add(partial);
							partial = out.createPartial();
						} else {
							// add to existing partial
							partial.addEmbedding(out.getEmbedding());
						}
					}
				}
				// add the last partial
				output.add(partial);

				long sinceLastTook = (System.currentTimeMillis() - start) + timeWaitingOnQueue;
				long backpressureWait = lastTookMs.get() - sinceLastTook;
				if (backpressureWait > 0) {
					// apply backpressure
					Thread.sleep(backpressureWait);
				}

				ElasticsearchService.BulkOpResponse res = esService
						.bulkUpdate(esConfig.getIndex(params.getOutputIndexRoot()), output);
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

		// NOTE: we want to upload _all_ embedding chunks in a single payload, so we
		// need to ensure when a worker receives the embeddings, it has all the
		// embeddings for a single document and it is not split between workers.

		readLinesIntoQueue(workQueue, Paths.get(params.getInputDir()).resolve("embeddings"),
				(String item) -> {
					try {
						return ingest.deserializeEmbedding(item);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				},
				(List<IInputEmbeddingChunk> chunk, IInputEmbeddingChunk latestToAdd) -> {
					// if we are under the batch size, don't chunk
					if (chunk.size() < params.getEmbeddingsBatchSize()) {
						return false;
					}

					// if we are over, only chunk if the newest item is for a different doc

					IInputEmbeddingChunk last = chunk.get(chunk.size() - 1);

					// do not chunk unless we have different doc ids
					return !last.getId().equals(latestToAdd.getId());
				});

		waitUntilWorkersAreDone(workQueue);

		return errors;
	}

}
