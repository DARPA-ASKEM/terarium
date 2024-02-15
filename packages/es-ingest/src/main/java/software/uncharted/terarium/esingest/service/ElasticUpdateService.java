package software.uncharted.terarium.esingest.service;

import java.io.IOException;
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
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticUpdateService extends ConcurrentWorkerService {

	private final ElasticsearchService esService;
	private final ElasticsearchConfiguration esConfig;

	public <Input extends IInputDocument, Output extends IOutputDocument> List<String> updateDocuments(
			ElasticIngestParams params,
			IElasticPass<Input, Output> ingest)
			throws IOException, InterruptedException, ExecutionException {

		List<String> errors = Collections.synchronizedList(new ArrayList<>());

		BlockingQueue<List<Input>> workQueue = new LinkedBlockingQueue<>(params.getWorkQueueSize());

		AtomicLong lastTookMs = new AtomicLong(0);
		startWorkers(workQueue, (List<Input> items, Long timeWaitingOnQueue) -> {
			try {
				long start = System.currentTimeMillis();

				List<Output> output = ingest.process(items);
				if (output.isEmpty()) {
					log.warn("Batch had not processed output, skipping");
					return;
				}

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

		readWorkIntoQueue(workQueue, ingest.getIterator(params));

		waitUntilWorkersAreDone(workQueue);

		return errors;
	}

}
