package software.uncharted.terarium.esingest.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

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

  protected <T extends IInputDocument> void startWorkers(
      BlockingQueue<List<T>> queue, BiConsumer<List<T>, Long> task) {
    for (int i = 0; i < POOL_SIZE; i++) {
      futures.add(
          executor.submit(
              () -> {
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

  protected <T extends IInputDocument> void waitUntilWorkersAreDone(BlockingQueue<List<T>> queue)
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

  protected <T extends IInputDocument> void readWorkIntoQueue(
      BlockingQueue<List<T>> queue, IInputIterator<T> iterator)
      throws IOException, InterruptedException {
    long lineCount = 0;
    while (true) {
      List<T> next = iterator.getNext();
      if (next == null) {
        // we are done
        log.info("No more work for queue");
        break;
      }
      if (shouldStop.get()) {
        throw new InterruptedException("Worker encountered an error, stopping ingest");
      }
      // push work to queue
      lineCount += next.size();
      log.info("Dispatching {} of {} total lines to work queue", next.size(), lineCount);
      queue.offer(next, WORK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
  }

  public void shutdown() {
    executor.shutdown();
  }
}
