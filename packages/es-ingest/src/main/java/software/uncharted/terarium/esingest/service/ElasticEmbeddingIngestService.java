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
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.models.input.IInputEmbedding;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputEmbeddingChunk;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticEmbeddingIngestService extends ConcurrentWorkerService {

	@Value("${terarium.esingest.workQueueSize:36}")
	private int WORK_QUEUE_SIZE;

	@Value("${terarium.esingest.errorThreshold:10}")
	private int ERROR_THRESHOLD;

	@Value("${terarium.esingest.embeddingBatchSize:500}")
	private int EMBEDDING_BATCH_SIZE;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ElasticsearchService esService;

	private List<String> errors = Collections.synchronizedList(new ArrayList<>());

	public <EmbeddingType, EmbeddingInputType extends IInputEmbedding, EmbeddingChunkType extends IOutputEmbeddingChunk<EmbeddingType>, DocumentOutputType extends IOutputDocument<EmbeddingType>> List<String> ingestData(
			ElasticIngestParams params,
			Function<EmbeddingInputType, EmbeddingChunkType> embeddingProcessor,
			Class<EmbeddingInputType> embeddingInputType)
			throws IOException, InterruptedException, ExecutionException {

		BlockingQueue<List<EmbeddingInputType>> workQueue = new LinkedBlockingQueue<>(WORK_QUEUE_SIZE);

		AtomicLong lastTookMs = new AtomicLong(0);
		startWorkers(workQueue, (List<EmbeddingInputType> items, Long timeWaitingOnQueue) -> {
			try {
				long start = System.currentTimeMillis();

				List<IOutputDocument<EmbeddingType>> output = new ArrayList<>();

				IOutputDocument<EmbeddingType> partial = null;
				for (EmbeddingInputType item : items) {
					EmbeddingChunkType out = embeddingProcessor.apply(item);
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

				ElasticsearchService.BulkOpResponse res = esService.bulkUpdate(params.getOutputIndex(), output);
				if (res.getErrors().size() > 0) {
					errors.addAll(res.getErrors());
					if (errors.size() > ERROR_THRESHOLD) {
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
						return objectMapper.readValue(item, embeddingInputType);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				},
				(List<EmbeddingInputType> chunk, EmbeddingInputType latestToAdd) -> {
					// if we are under the batch size, don't chunk
					if (chunk.size() < EMBEDDING_BATCH_SIZE) {
						return false;
					}

					// if we are over, only chunk if the newest item is for a different doc

					EmbeddingInputType last = chunk.get(chunk.size() - 1);

					// do not chunk unless we have different doc ids
					return !last.getId().equals(latestToAdd.getId());
				});

		waitUntilWorkersAreDone(workQueue);

		return errors;
	}

}
