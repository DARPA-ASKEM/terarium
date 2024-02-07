package software.uncharted.terarium.taskrunner.service;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;
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
	private String outputPipeName;
	private TaskStatus status = TaskStatus.QUEUED;
	private ScopedLock lock = new ScopedLock();
	private int NUM_THREADS = 8;
	ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

	private int PROCESS_KILL_TIMEOUT_SECONDS = 10;

	private int BYTES_PER_READ = 1024 * 1024;

	public Task(TaskRequest req) throws IOException, InterruptedException {
		mapper = new ObjectMapper();

		this.req = req;
		inputPipeName = "/tmp/input-" + req.getId();
		outputPipeName = "/tmp/output-" + req.getId();

		try {
			setup();
		} catch (Exception e) {
			cleanup();
			throw e;
		}
	}

	public UUID getId() {
		return req.getId();
	}

	public TaskResponse createResponse(TaskStatus status) {
		return req.createResponse(status);
	}

	private String getExtension(String input) {
		int i = input.lastIndexOf('.');
		if (i > 0) {
			return input.substring(i + 1);
		}
		return "";
	}

	private void setup() throws IOException, InterruptedException {
		if (getExtension(req.getScript()).equals("py")) {
			// raw python file, execute it through the runtime
			boolean fileExists = Files.exists(Paths.get(req.getScript()));
			if (!fileExists) {
				throw new FileNotFoundException("Script file: " + req.getScript() + " not found");
			}
			processBuilder = new ProcessBuilder("python", req.getScript(), "--id", req.getId().toString(),
					"--input_pipe",
					inputPipeName,
					"--output_pipe", outputPipeName);
		} else {
			// executable command, execute it directly
			processBuilder = new ProcessBuilder(req.getScript(), "--id", req.getId().toString(),
					"--input_pipe",
					inputPipeName,
					"--output_pipe", outputPipeName);
		}

		log.debug("Creating input and output pipes: {} {} for task {}", inputPipeName, outputPipeName, req.getId());

		// Create the named pipes
		Process inputPipe = new ProcessBuilder("mkfifo", inputPipeName).start();
		int exitCode = inputPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}

		Process outputPipe = new ProcessBuilder("mkfifo", outputPipeName).start();
		exitCode = outputPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}
	}

	public void writeInputWithTimeout(byte[] bytes, int timeoutMinutes)
			throws IOException, InterruptedException, TimeoutException {
		log.debug("Dispatching write thread for input pipe: {} for task: {}", inputPipeName, req.getId());

		CompletableFuture<Void> future = new CompletableFuture<>();
		new Thread(() -> {
			try {
				// Write to the named pipe in a separate thread
				log.debug("Opening input pipe: {} for task: {}", inputPipeName, req.getId());
				try (FileOutputStream fos = new FileOutputStream(inputPipeName)) {
					log.debug("Writing to input pipe: {} for task: {}", inputPipeName, req.getId());
					fos.write(bytes);
				}
				future.complete(null);
			} catch (IOException e) {
				future.completeExceptionally(e);
			}
		}).start();

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException("Error while writing to pipe", e);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new TimeoutException("Writing to pipe took too long for task " + req.getId());
		}

		if (result == null) {
			// successful write
			return;
		}
		if (result instanceof Integer) {
			// process has exited early
			if (getStatus() == TaskStatus.CANCELLED) {
				throw new InterruptedException("Process for task " + req.getId() + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + req.getId() + " exited early with code " + result);
		}
		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	public byte[] readOutputWithTimeout(int timeoutMinutes)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		log.debug("Dispatching read thread for input pipe: {} for task: {}", outputPipeName, req.getId());

		CompletableFuture<byte[]> future = new CompletableFuture<>();
		new Thread(() -> {
			log.debug("Opening output pipe: {} for task: {}", outputPipeName, req.getId());
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(outputPipeName))) {
				log.debug("Reading from output pipe: {} for task: {}", outputPipeName, req.getId());
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buffer = new byte[BYTES_PER_READ]; // buffer size
				int bytesRead;
				while ((bytesRead = bis.read(buffer)) != -1) {
					log.debug("Read {} bytes from output pipe: {} for task: {}", bytesRead, outputPipeName,
							req.getId());
					bos.write(buffer, 0, bytesRead);
				}
				future.complete(bos.toByteArray());
			} catch (IOException e) {
				future.completeExceptionally(e);
			}

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(outputPipeName)))) {
				log.debug("Reading on output pipe: {} for task {}", outputPipeName, req.getId());
				future.complete(reader.readLine().getBytes());
			} catch (IOException e) {
				future.completeExceptionally(e);
			}
		}).start();

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
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
			if (getStatus() == TaskStatus.CANCELLED) {
				throw new InterruptedException("Process for task " + req.getId() + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + req.getId() + " exited early with code " + result);
		}

		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	public void cleanup() {
		try {
			Files.deleteIfExists(Paths.get(inputPipeName));
		} catch (Exception e) {
			log.warn("Exception occurred while cleaning up the task input pipe:" + e);
		}

		try {
			Files.deleteIfExists(Paths.get(outputPipeName));
		} catch (Exception e) {
			log.warn("Exception occurred while cleaning up the task output pipe:" + e);
		}

		try {
			cancel();
		} catch (Exception e) {
			log.warn("Exception occurred while killing any residual process:" + e);
		}
	}

	public void start() throws IOException, InterruptedException {

		lock.lock();
		try {
			if (status == TaskStatus.CANCELLED) {
				// don't run if we already cancelled
				throw new InterruptedException("Task " + req.getId() + "has already been cancelled");
			}

			if (status != TaskStatus.QUEUED) {
				// has to be in a queued state to be valid to run
				throw new RuntimeException("Task " + req.getId() + " has already been started");
			}

			log.info("Starting task {} executing {}", req.getId(), req.getScript());
			process = processBuilder.start();

			// flag as running if the process starts
			status = TaskStatus.RUNNING;

			// Add a shutdown hook to kill the process if the JVM exits
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				process.destroy();
			}));

			// Create a future to signal when the process has exited
			processFuture = new CompletableFuture<>();
			new Thread(() -> {
				try {
					log.debug("Begin waiting for process to exit for task {}");
					int exitCode = process.waitFor();
					log.info("Process exited with code {} for task {}", exitCode, req.getId());
					lock.lock(() -> {
						if (exitCode != 0) {
							if (status == TaskStatus.CANCELLING) {
								status = TaskStatus.CANCELLED;
							} else {
								status = TaskStatus.FAILED;
							}
						} else {
							status = TaskStatus.SUCCESS;
						}
					});
					log.debug("Finalized process status for task {}", exitCode, req.getId());
					processFuture.complete(exitCode);
				} catch (InterruptedException e) {
					log.warn("Process failed to exit cleanly for task {}: {}", req.getId(), e);
					lock.lock(() -> {
						status = TaskStatus.FAILED;
					});
					processFuture.completeExceptionally(e);
				}
			}).start();

			InputStream inputStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();

			new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
					String line;
					while ((line = reader.readLine()) != null) {
						log.info("[{}] stdout: {}", req.getId(), line);
					}
				} catch (IOException e) {
					log.warn("Error occured while logging stdout for task {}: {}", req.getId(),
							getStatus());
				}
			}).start();

			new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
					String line;
					while ((line = reader.readLine()) != null) {
						log.warn("[{}] stderr: {}", req.getId(), line);
					}
				} catch (IOException e) {
					log.warn("Error occured while logging stderr for task {}: {}", req.getId(), getStatus());
				}
			}).start();

		} catch (Exception e) {
			status = TaskStatus.FAILED;
			throw e;
		} finally {
			lock.unlock();
		}
	}

	public void waitFor(int timeoutMinutes) throws InterruptedException, TimeoutException, ExecutionException {
		boolean hasExited = process.waitFor((long) timeoutMinutes, TimeUnit.MINUTES);
		if (hasExited) {
			// if we have exited, lets wait on the future to resolve and the status to be
			// correctly set
			processFuture.get();
			int exitCode = process.exitValue();
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
			if (status == TaskStatus.QUEUED) {
				// if we havaen't started yet, flag it as cancelled
				log.debug("Cancelled task {} before starting it", req.getId());
				status = TaskStatus.CANCELLED;
				return false;
			}
			if (status != TaskStatus.RUNNING) {
				// can't cancel a process if it isn't in a running state
				return false;
			}

			status = TaskStatus.CANCELLING;
			return true;
		});
	}

	public boolean cancel() {
		flagAsCancelling();

		if (getStatus() != TaskStatus.CANCELLING) {
			return false;
		}

		long start = System.currentTimeMillis();

		if (process == null) {
			throw new RuntimeException("Process is null for task: " + req.getId());
		}

		// try to kill cleanly
		log.info("Cancelling task {}", req.getId());
		process.destroy();

		try {
			processFuture.get(PROCESS_KILL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

			log.info("Process successfully cancelled in: {} for task {}", req.getId(),
					TimeFormatter.format(System.currentTimeMillis() - start), req.getId());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.warn("Error while waiting for task {} process to exit cleanly in {}, sending SIGKILL", req.getId(),
					TimeFormatter.format(System.currentTimeMillis() - start));
			// kill the process forcibly (SIGKILL)
			process.destroyForcibly();
		}

		return true;
	}

	TaskStatus getStatus() {
		return lock.lock(() -> {
			return status;
		});
	}

}
