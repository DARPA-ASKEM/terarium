package software.uncharted.terarium.esingest.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcurrentWorkerService {

	@Value("${terarium.esingest.workerPoolSize:8}")
	private int POOL_SIZE;

	@Value("${terarium.esingest.workTimeoutSeconds:60}")
	private int WORK_TIMEOUT_SECONDS;

	private ExecutorService executor;
	private List<Future<Void>> futures = new ArrayList<>();
	private AtomicBoolean shouldStop = new AtomicBoolean(false);

	@PostConstruct
	void init() {
		executor = Executors.newFixedThreadPool(POOL_SIZE);
	}

	protected List<Path> getFilesInDir(Path dir) {
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

	protected <T> void startWorkers(BlockingQueue<List<T>> queue, BiConsumer<List<T>, Long> task) {
		for (int i = 0; i < POOL_SIZE; i++) {
			futures.add(executor.submit(() -> {
				while (true) {
					try {
						long start = System.currentTimeMillis();
						List<T> args = queue.take();
						if (args.size() == 0) {
							break;
						}
						task.accept(args, System.currentTimeMillis() - start);

					} catch (Exception e) {
						log.error("Error processing work", e);
						shouldStop.set(true);
						throw e;
					}
				}
				return null;
			}));
		}
	}

	protected <T> void waitUntilWorkersAreDone(BlockingQueue<List<T>> queue)
			throws InterruptedException, ExecutionException {

		// now lets dispatch the worker kill signals (empty lists)
		for (int i = 0; i < POOL_SIZE; i++) {
			queue.offer(new ArrayList<>(), WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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

	protected void readLinesIntoQueue(BlockingQueue<List<String>> queue, int batchSize, Path p)
			throws InterruptedException {
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
					if (lines.size() == batchSize) {
						lineCount += lines.size();
						log.info("Dispatching {} of {} total lines to work queue", lines.size(), lineCount);
						queue.offer(lines, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
						lines = new ArrayList<>();
					}
				}
				// process the remaining lines if there are any
				if (!lines.isEmpty()) {
					lineCount += lines.size();
					log.info("Dispatching remaining {} of {} total lines to work queue", lines.size(), lineCount);
					lineCount += lines.size();
					queue.offer(lines, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				}
			} catch (IOException e) {
				log.error("Error reading file", e);
			}
		}
	}

	public <T> void readLinesIntoQueue(BlockingQueue<List<T>> queue, Path p,
			Function<String, T> processor, BiFunction<List<T>, T, Boolean> chunker)
			throws InterruptedException {
		List<Path> paths = getFilesInDir(p);
		long lineCount = 0;
		for (Path path : paths) {
			// read the file and put the lines into the work queue
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				List<T> chunk = new ArrayList<>();
				for (String line; (line = reader.readLine()) != null;) {
					if (shouldStop.get()) {
						throw new InterruptedException("Worker encountered an error, stopping ingest");
					}

					// process the line
					T processed = processor.apply(line);

					// check if we need to split the chunk
					boolean splitChunk = chunker.apply(chunk, processed);

					if (splitChunk) {
						lineCount += chunk.size();
						log.info("Dispatching {} of {} total lines to work queue", chunk.size(),
								lineCount);
						queue.offer(chunk, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
						chunk = new ArrayList<>();
					}
					chunk.add(processed);
				}
				// process the remaining lines if there are any
				if (!chunk.isEmpty()) {
					lineCount += chunk.size();
					log.info("Dispatching remaining {} of {} total lines to work queue",
							chunk.size(), lineCount);
					lineCount += chunk.size();
					queue.offer(chunk, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				}
			} catch (IOException e) {
				log.error("Error reading file", e);
			}
		}
	}

	public void shutdown() {
		executor.shutdown();
	}

}
