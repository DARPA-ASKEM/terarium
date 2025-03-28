package software.uncharted.terarium.taskrunner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.taskrunner.models.task.ProgressState;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;
import software.uncharted.terarium.taskrunner.util.ScopedLock;
import software.uncharted.terarium.taskrunner.util.TimeFormatter;

@Data
@Slf4j
public class Task {

	private TaskRequest req;
	private ObjectMapper mapper;
	private ProcessBuilder processBuilder;
	private Process process;
	private CompletableFuture<Integer> processFuture;
	private String inputPipeName;
	private String progressPipeName;
	private String outputPipeName;
	private ProgressState status = ProgressState.QUEUED;
	private ScopedLock lock = new ScopedLock();
	private StringBuilder stdout = new StringBuilder();
	private StringBuilder stderr = new StringBuilder();

	private final String ECHO_SCRIPT_PATH = getClass().getResource("/echo.py").getPath();

	private int PROCESS_KILL_TIMEOUT_SECONDS = 10;

	private int BYTES_PER_READ = 1024 * 1024;

	public Task(final TaskRequest req) throws IOException, InterruptedException {
		mapper = new ObjectMapper();

		this.req = req;
		inputPipeName = "/tmp/input-" + req.getId();
		progressPipeName = "/tmp/progress-" + req.getId();
		outputPipeName = "/tmp/output-" + req.getId();

		try {
			setup();
		} catch (final Exception e) {
			cleanup();
			throw e;
		}
	}

	public UUID getId() {
		return req.getId();
	}

	public TaskResponse createResponse(final ProgressState status) {
		return req.createResponse(status, stdout.toString(), stderr.toString());
	}

	private String getExtension(final String input) {
		final int i = input.lastIndexOf('.');
		if (i > 0) {
			return input.substring(i + 1);
		}
		return "";
	}

	private void setup() throws IOException, InterruptedException {
		if (req.getScript().equals("echo.py")) {
			// raw python file, execute it through the runtime
			final boolean fileExists = Files.exists(Paths.get(ECHO_SCRIPT_PATH));
			if (!fileExists) {
				throw new FileNotFoundException("Script file: " + ECHO_SCRIPT_PATH + " not found");
			}
			processBuilder = new ProcessBuilder(
				"python3",
				ECHO_SCRIPT_PATH,
				"--id",
				req.getId().toString(),
				"--input_pipe",
				inputPipeName,
				"--output_pipe",
				outputPipeName,
				"--progress_pipe",
				progressPipeName
			);
		} else if (getExtension(req.getScript()).equals("py")) {
			// raw python file, execute it through the runtime
			final boolean fileExists = Files.exists(Paths.get(req.getScript()));
			if (!fileExists) {
				throw new FileNotFoundException("Script file: " + req.getScript() + " not found");
			}
			processBuilder = new ProcessBuilder(
				"python3",
				req.getScript(),
				"--id",
				req.getId().toString(),
				"--input_pipe",
				inputPipeName,
				"--output_pipe",
				outputPipeName,
				"--progress_pipe",
				progressPipeName
			);
		} else {
			// executable command, execute it directly
			processBuilder = new ProcessBuilder(
				req.getScript(),
				"--id",
				req.getId().toString(),
				"--input_pipe",
				inputPipeName,
				"--output_pipe",
				outputPipeName,
				"--progress_pipe",
				progressPipeName
			);
		}

		log.info(
			"Creating input, output, and progress pipes: {}, {}, {} for task {}",
			inputPipeName,
			outputPipeName,
			progressPipeName,
			req.getId()
		);

		// Create the named pipes
		final Process inputPipe = new ProcessBuilder("mkfifo", inputPipeName).start();
		int exitCode = inputPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}

		final Process outputPipe = new ProcessBuilder("mkfifo", outputPipeName).start();
		exitCode = outputPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}

		final Process progressPipe = new ProcessBuilder("mkfifo", progressPipeName).start();
		exitCode = progressPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}
	}

	public void writeInputWithTimeout(final byte[] bytes, final int timeoutMinutes)
		throws IOException, InterruptedException, TimeoutException {
		log.info("Dispatching write thread for input pipe: {} for task: {}", inputPipeName, req.getId());

		final CompletableFuture<Void> future = new CompletableFuture<>();
		new Thread(() -> {
			try {
				// Write to the named pipe in a separate thread
				log.info("Opening input pipe: {} for task: {}", inputPipeName, req.getId());
				try (FileOutputStream fos = new FileOutputStream(inputPipeName)) {
					log.info("Writing to input pipe: {} for task: {}", inputPipeName, req.getId());
					fos.write(bytes);
				}
				future.complete(null);
			} catch (final IOException e) {
				future.completeExceptionally(e);
			}
		}).start();

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException("Error while writing to pipe", e);
		} catch (final TimeoutException e) {
			future.cancel(true);
			throw new TimeoutException("Writing to pipe took too long for task " + req.getId());
		}

		if (result == null) {
			// successful write
			return;
		}
		if (result instanceof Integer) {
			// process has exited early
			if (getStatus() == ProgressState.CANCELLED) {
				throw new InterruptedException("Process for task " + req.getId() + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + req.getId() + " exited early with code " + result);
		}
		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	public byte[] readOutputWithTimeout(final int timeoutMinutes)
		throws IOException, InterruptedException, ExecutionException, TimeoutException {
		log.info("Dispatching read thread for output pipe: {} for task: {}", outputPipeName, req.getId());

		final CompletableFuture<byte[]> future = new CompletableFuture<>();
		new Thread(() -> {
			log.info("Opening output pipe: {} for task: {}", outputPipeName, req.getId());
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(outputPipeName))) {
				log.info("Reading from output pipe: {} for task: {}", outputPipeName, req.getId());
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				final byte[] buffer = new byte[BYTES_PER_READ]; // buffer size
				int bytesRead;
				while ((bytesRead = bis.read(buffer)) != -1) {
					log.info("Read {} bytes from output pipe: {} for task: {}", bytesRead, outputPipeName, req.getId());
					bos.write(buffer, 0, bytesRead);
				}
				future.complete(bos.toByteArray());
			} catch (final IOException e) {
				future.completeExceptionally(e);
			}
		}).start();

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (final TimeoutException e) {
			future.cancel(true);
			throw new TimeoutException("Reading from pipe took too long for task " + req.getId());
		}

		if (result == null) {
			throw new RuntimeException("Unexpected null result for task " + req.getId());
		}

		if (result instanceof byte[]) {
			// we got our response
			return (byte[]) result;
		}
		if (result instanceof Integer) {
			// process has exited early
			if (getStatus() == ProgressState.CANCELLED) {
				throw new InterruptedException("Process for task " + req.getId() + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + req.getId() + " exited early with code " + result);
		}

		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	public byte[] readProgressWithTimeout(final int timeoutMinutes)
		throws IOException, InterruptedException, ExecutionException, TimeoutException {
		log.info("Dispatching read thread for progress pipe: {} for task: {}", progressPipeName, req.getId());

		final CompletableFuture<byte[]> future = new CompletableFuture<>();
		new Thread(() -> {
			log.info("Opening progress pipe: {} for task: {}", progressPipeName, req.getId());
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(progressPipeName))) {
				log.info("Reading from progress pipe: {} for task: {}", progressPipeName, req.getId());
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				final byte[] buffer = new byte[BYTES_PER_READ]; // buffer size
				int bytesRead;
				while ((bytesRead = bis.read(buffer)) != -1) {
					log.info("Read {} bytes from progress pipe: {} for task: {}", bytesRead, progressPipeName, req.getId());
					bos.write(buffer, 0, bytesRead);
				}
				future.complete(bos.toByteArray());
			} catch (final IOException e) {
				future.completeExceptionally(e);
			}
		}).start();

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (final TimeoutException e) {
			future.cancel(true);
			throw new TimeoutException("Reading from pipe took too long for task " + req.getId());
		}

		if (result == null) {
			throw new RuntimeException("Unexpected null result for task " + req.getId());
		}

		if (result instanceof byte[]) {
			// we got our response
			try {
				final JsonNode progress = mapper.readTree((byte[]) result);
				if (progress.has("done")) {
					// finished reading progress
					return null;
				}
			} catch (final Exception e) {
				// do nothing
			}
			return (byte[]) result;
		}
		if (result instanceof Integer) {
			// process has exited early
			if (getStatus() == ProgressState.CANCELLED) {
				throw new InterruptedException("Process for task " + req.getId() + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + req.getId() + " exited early with code " + result);
		}

		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	public void cleanup() {
		try {
			Files.deleteIfExists(Paths.get(inputPipeName));
		} catch (final Exception e) {
			log.warn("Exception occurred while cleaning up the task input pipe:" + e);
		}

		try {
			Files.deleteIfExists(Paths.get(outputPipeName));
		} catch (final Exception e) {
			log.warn("Exception occurred while cleaning up the task output pipe:" + e);
		}

		try {
			Files.deleteIfExists(Paths.get(progressPipeName));
		} catch (final Exception e) {
			log.warn("Exception occurred while cleaning up the task progress pipe:" + e);
		}

		try {
			cancel();
		} catch (final Exception e) {
			log.warn("Exception occurred while killing any residual process:" + e);
		}
	}

	public void start() throws IOException, InterruptedException {
		lock.lock();
		try {
			if (status == ProgressState.CANCELLED) {
				// don't run if we already cancelled
				throw new InterruptedException("Task " + req.getId() + "has already been cancelled");
			}

			if (status != ProgressState.QUEUED) {
				// has to be in a queued state to be valid to run
				throw new RuntimeException("Task " + req.getId() + " has already been started");
			}

			log.info("Starting task {} executing {}", req.getId(), req.getScript());
			process = processBuilder.start();

			// flag as running if the process starts
			status = ProgressState.RUNNING;

			// Add a shutdown hook to kill the process if the JVM exits
			Runtime.getRuntime()
				.addShutdownHook(
					new Thread(() -> {
						process.destroy();
					})
				);

			// Create a future to signal when the process has exited
			processFuture = new CompletableFuture<>();
			new Thread(() -> {
				try {
					log.info("Begin waiting for process to exit for task {}", req.getId());
					final int exitCode = process.waitFor();
					log.info("Process exited with code {} for task {}", exitCode, req.getId());
					lock.lock(() -> {
						if (exitCode != 0) {
							if (status == ProgressState.CANCELLED) {
								status = ProgressState.CANCELLED;
							} else {
								status = ProgressState.ERROR;
							}
						} else {
							status = ProgressState.COMPLETE;
						}
					});
					log.info("Finalized process status for task {}", exitCode, req.getId());
					processFuture.complete(exitCode);
				} catch (final InterruptedException e) {
					log.warn("Process failed to exit cleanly for task {}: {}", req.getId(), e);
					lock.lock(() -> {
						status = ProgressState.ERROR;
					});
					processFuture.completeExceptionally(e);
				}
			}).start();

			final InputStream inputStream = process.getInputStream();
			final InputStream errorStream = process.getErrorStream();

			new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
					String line;
					while ((line = reader.readLine()) != null) {
						log.info("[{}] stdout: {}", req.getId(), line);
						stdout.append(line).append("\n");
					}
				} catch (final IOException e) {
					log.warn("Error occured while logging stdout for task {}: {}", req.getId(), getStatus());
				}
			}).start();

			new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
					String line;
					while ((line = reader.readLine()) != null) {
						log.warn("[{}] stderr: {}", req.getId(), line);
						stderr.append(line).append("\n");
					}
				} catch (final IOException e) {
					log.warn("Error occured while logging stderr for task {}: {}", req.getId(), getStatus());
				}
			}).start();
		} catch (final Exception e) {
			if (status != ProgressState.CANCELLED) {
				status = ProgressState.ERROR;
			}
			throw e;
		} finally {
			lock.unlock();
		}
	}

	public void waitFor(final int timeoutMinutes) throws InterruptedException, TimeoutException, ExecutionException {
		final boolean hasExited = process.waitFor((long) timeoutMinutes, TimeUnit.MINUTES);
		if (hasExited) {
			// if we have exited, lets wait on the future to resolve and the status to be
			// correctly set
			processFuture.get();
			final int exitCode = process.exitValue();
			if (exitCode != 0) {
				throw new RuntimeException("Python script exited with non-zero exit code: " + exitCode);
			}
		} else {
			throw new TimeoutException("Process did not exit within the timeout");
		}
	}

	public boolean flagAsCancelling() {
		// Splitting this off as separate method allows us to accept a cancel
		// request, response that we are cancelling, and then process it.

		return lock.lock(() -> {
			if (status == ProgressState.QUEUED) {
				// if we havaen't started yet, flag it as cancelled
				log.info("Cancelled task {} before starting it", req.getId());
				status = ProgressState.CANCELLED;
				return false;
			}
			if (status != ProgressState.RUNNING) {
				// can't cancel a process if it isn't in a running state
				return false;
			}

			status = ProgressState.CANCELLED;
			return true;
		});
	}

	public boolean cancel() {
		flagAsCancelling();

		if (getStatus() != ProgressState.CANCELLED) {
			return false;
		}

		final long start = System.currentTimeMillis();

		if (process == null) {
			throw new RuntimeException("Process is null for task: " + req.getId());
		}

		// try to kill cleanly
		log.info("Cancelling task {}", req.getId());
		process.destroy();

		try {
			processFuture.get(PROCESS_KILL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

			log.info(
				"Process successfully cancelled in: {} for task {}",
				req.getId(),
				TimeFormatter.format(System.currentTimeMillis() - start),
				req.getId()
			);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.warn(
				"Error while waiting for task {} process to exit cleanly in {}, sending SIGKILL",
				req.getId(),
				TimeFormatter.format(System.currentTimeMillis() - start)
			);
			// kill the process forcibly (SIGKILL)
			process.destroyForcibly();
		}

		return true;
	}

	ProgressState getStatus() {
		return lock.lock(() -> {
			return status;
		});
	}
}
