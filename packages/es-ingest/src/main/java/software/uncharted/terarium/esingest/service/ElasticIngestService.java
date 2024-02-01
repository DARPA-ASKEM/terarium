package software.uncharted.terarium.esingest.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.json.JsonData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.models.input.InputInterface;
import software.uncharted.terarium.esingest.models.output.OutputInterface;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticIngestService {

	@Value("${terarium.esingest.workQueueSize:36}")
	private int WORK_QUEUE_SIZE;

	@Value("${terarium.esingest.errorThreshold:10}")
	private int ERROR_THRESHOLD;

	@Value("${terarium.esingest.bulkSize:100}")
	private int BULK_SIZE;

	@Value("${terarium.esingest.workerPoolSize:8}")
	private int POOL_SIZE;

	@Value("${terarium.esingest.workTimeoutSeconds:60}")
	private int WORK_TIMEOUT_SECONDS;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ElasticsearchService esService;

	private List<String> errors = Collections.synchronizedList(new ArrayList<>());

	private BlockingQueue<List<String>> workQueue;// = new LinkedBlockingQueue<>(WORK_QUEUE_SIZE);
	private ExecutorService executor;// = Executors.newFixedThreadPool(POOL_SIZE);
	private List<Future<Void>> futures = new ArrayList<>();

	private ElasticIngestParams params;

	private AtomicBoolean shouldStop = new AtomicBoolean(false);

	@PostConstruct
	void init() {
		workQueue = new LinkedBlockingQueue<>(WORK_QUEUE_SIZE);
		executor = Executors.newFixedThreadPool(POOL_SIZE);
	}

	private List<Path> getFilesInDir(Path dir) {
		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path file : stream) {
				// Process the file here
				// For example, you can print the filename
				System.out.println(file.getFileName());
				files.add(file);
			}
		} catch (IOException e) {
			log.error("Error reading directory", e);
		}
		return files;
	}

	private <InputType extends InputInterface, OutputType extends OutputInterface> void startIngestDocumentWorkers(
			Function<InputType, OutputType> processor,
			Class<InputType> inputType) {
		for (int i = 0; i < POOL_SIZE; i++) {
			futures.add(executor.submit(() -> {
				long lastTook = 0;
				while (true) {
					try {
						long start = System.currentTimeMillis();
						List<String> items = workQueue.take();
						if (items.size() == 0) {
							break;
						}

						List<OutputType> output = new ArrayList<>();
						for (String item : items) {
							InputType input = objectMapper.readValue(item, inputType);
							OutputType out = processor.apply(input);
							if (out != null) {
								output.add(out);
							}
						}

						long sinceLastTook = System.currentTimeMillis() - start;
						long backpressureWait = lastTook - sinceLastTook;
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
						lastTook = res.getTook();

					} catch (Exception e) {
						log.error("Error processing documents", e);
						shouldStop.set(true);
						throw e;
					}
				}
				return null;
			}));
		}
	}

	private <InputType extends InputInterface, OutputType extends OutputInterface> void startIngestEmbeddingsWorkers(
			Function<InputType, OutputType> processor,
			Class<InputType> inputType) {

		Thread parentThread = Thread.currentThread();

		for (int i = 0; i < POOL_SIZE; i++) {
			futures.add(executor.submit(() -> {
				long lastTook = 0;
				while (true) {
					try {
						long start = System.currentTimeMillis();
						List<String> items = workQueue.take();
						if (items.size() == 0) {
							break;
						}

						List<ElasticsearchService.ScriptedUpdatedDoc> output = new ArrayList<>();
						for (String item : items) {
							InputType input = objectMapper.readValue(item, inputType);
							OutputType out = processor.apply(input);
							if (out != null) {

								// generic way to extract the id
								String jsonString = objectMapper.writeValueAsString(out);
								JsonData jsonData = JsonData.fromJson(jsonString);

								ElasticsearchService.ScriptedUpdatedDoc doc = new ElasticsearchService.ScriptedUpdatedDoc();
								doc.setId(out.getId().toString());
								doc.setParams(Map.of("paragraph", jsonData));
								output.add(doc);
							}
						}

						String script = """
								if (ctx._source.paragraphs == null) {
									ctx._source.paragraphs = new ArrayList();
								}
								ctx._source.paragraphs.add(params.paragraph);""";

						long sinceLastTook = System.currentTimeMillis() - start;
						long backpressureWait = lastTook - sinceLastTook;
						if (backpressureWait > 0) {
							// apply backpressure
							Thread.sleep(backpressureWait);
						}

						ElasticsearchService.BulkOpResponse res = esService.bulkScriptedUpdate(params.getOutputIndex(),
								script, output);
						if (res.getErrors().size() > 0) {
							errors.addAll(res.getErrors());
							if (errors.size() > ERROR_THRESHOLD) {
								for (String err : errors) {
									log.error(err);
								}
								throw new InterruptedException("Too many errors, stopping ingest");
							}
						}
						lastTook = res.getTook();

					} catch (Exception e) {
						log.error("Error processing documents", e);
						shouldStop.set(true);
						parentThread.interrupt(); // break the parent thread out of blocking on the queue
						throw e;
					}
				}
				return null;
			}));
		}
	}

	private void waitUntilWorkersAreDone() throws InterruptedException, ExecutionException {

		// now lets dispatch the worker kill signals (empty lists)
		for (int i = 0; i < POOL_SIZE; i++) {
			workQueue.offer(new ArrayList<>(), WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}

		// now we wait for them to finish
		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				log.error("Error waiting on workers to finish", e);
				throw e;
			}
		}

		futures.clear();
	}

	private void readLinesIntoWorkQueue(Path p) throws InterruptedException {
		List<Path> paths = getFilesInDir(p);
		long lineCount = 0;
		for (Path path : paths) {
			// read the file and put the lines into the work queue
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				List<String> lines = new ArrayList<>();
				for (String line; (line = reader.readLine()) != null;) {
					if (shouldStop.get()) {
						throw new InterruptedException("Worker encountered an error, stopping ingest");
					}
					lines.add(line);
					if (lines.size() == BULK_SIZE) {
						lineCount += lines.size();
						log.info("Dispatching {} of {} total lines to work queue", lines.size(), lineCount);
						workQueue.offer(lines, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
						lines = new ArrayList<>();
					}
				}
				// process the remaining lines if there are any
				if (!lines.isEmpty()) {
					lineCount += lines.size();
					log.info("Dispatching remaining {} of {} total lines to work queue", lines.size(), lineCount);
					lineCount += lines.size();
					workQueue.offer(lines, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				}
			} catch (IOException e) {
				log.error("Error reading file", e);
			}
		}
	}

	public <DocInputType extends InputInterface, EmbeddingInputType extends InputInterface, DocOutputType extends OutputInterface, EmbeddingOutputType extends OutputInterface> List<String> ingestData(
			ElasticIngestParams params,
			Function<DocInputType, DocOutputType> docProcessor,
			Function<EmbeddingInputType, EmbeddingOutputType> embeddingProcessor,
			Class<DocInputType> docInputType,
			Class<EmbeddingInputType> embeddingInputType)
			throws IOException, InterruptedException, ExecutionException {

		this.params = params;

		// clear out the index:

		esService.createOrEnsureIndexIsEmpty(params.getOutputIndex());

		// first we insert the documents

		startIngestDocumentWorkers(docProcessor, docInputType);

		readLinesIntoWorkQueue(Paths.get(params.getInputDir()).resolve("documents"));

		waitUntilWorkersAreDone();

		// then we insert the embeddings

		startIngestEmbeddingsWorkers(embeddingProcessor, embeddingInputType);

		readLinesIntoWorkQueue(Paths.get(params.getInputDir()).resolve("embeddings"));

		waitUntilWorkersAreDone();

		return errors;

	}

}
