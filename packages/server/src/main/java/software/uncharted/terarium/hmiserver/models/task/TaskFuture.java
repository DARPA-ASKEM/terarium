package software.uncharted.terarium.hmiserver.models.task;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
public class TaskFuture implements Serializable {

	protected UUID id;
	protected TaskResponse latestResponse;
	protected CompletableFuture<TaskResponse> future;

	public UUID getId() {
		return id;
	}

	public synchronized TaskResponse poll() {
		return latestResponse;
	}

	public TaskResponse get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	public TaskResponse get(final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}
}
