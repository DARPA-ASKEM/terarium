package software.uncharted.terarium.hmiserver.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskManager {

	private ThreadPoolTaskExecutor executor;
	private Map<UUID, Future<?>> taskTable = new HashMap();

	public TaskManager() {
		executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("GithubLookup-");
		executor.initialize();
	}

	public void submitTask(final UUID id, Runnable r) {
		final Future futureTask = executor.submit(r);
		taskTable.put(id, futureTask);
	}

	public void cancelTask(final UUID id) {
		final Future futureTask = taskTable.get(id);
		if (futureTask == null) return;
		if (futureTask.isDone() || futureTask.isCancelled()) {
			taskTable.remove(id);
		} else {
			System.out.println("cancelling task " + id);
			futureTask.cancel(true);
		}
	}
}
