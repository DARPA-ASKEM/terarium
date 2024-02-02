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
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticDocumentIngestService extends ConcurrentWorkerService {

	@Value("${terarium.esingest.workQueueSize:36}")
	private int WORK_QUEUE_SIZE;

	@Value("${terarium.esingest.errorThreshold:10}")
	private int ERROR_THRESHOLD;

	@Value("${terarium.esingest.documentBatchSize:500}")
	private int DOCUMENT_BATCH_SIZE;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ElasticsearchService esService;
	private final List<String> errors = Collections.synchronizedList(new ArrayList<>());

	public <DocInputType extends IInputDocument, DocOutputType extends IOutputDocument<?>> List<String> ingestData(
			ElasticIngestParams params,
			Function<DocInputType, DocOutputType> docProcessor,
			Class<DocInputType> docInputType)
			throws IOException, InterruptedException, ExecutionException {

		BlockingQueue<List<String>> workQueue = new LinkedBlockingQueue<>(WORK_QUEUE_SIZE);

		AtomicLong lastTookMs = new AtomicLong(0);

		startWorkers(workQueue, (List<String> items, Long timeWaitingOnQueue) -> {
			try {
				long start = System.currentTimeMillis();
				List<DocOutputType> output = new ArrayList<>();
				for (String item : items) {
					DocInputType input = objectMapper.readValue(item, docInputType);
					DocOutputType out = docProcessor.apply(input);
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

				ElasticsearchService.BulkOpResponse res = esService.bulkIndex(params.getOutputIndex(), output);
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

		readLinesIntoQueue(workQueue, DOCUMENT_BATCH_SIZE, Paths.get(params.getInputDir()).resolve("documents"));

		waitUntilWorkersAreDone(workQueue);

		return errors;
	}

}
