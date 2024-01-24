package software.uncharted.terarium.taskrunner.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;
import software.uncharted.terarium.taskrunner.util.ScopedLock;

@Data
@Slf4j
public class Task {

	private UUID id;
	private String taskKey;
	private ObjectMapper mapper;
	private ProcessBuilder processBuilder;
	private Process process;
	private CompletableFuture<Integer> processFuture;
	private String inputPipeName;
	private String outputPipeName;
	private TaskStatus status = TaskStatus.QUEUED;
	private ScopedLock lock = new ScopedLock();
	private String script;

	public Task(UUID id, String taskKey) throws IOException, InterruptedException {
		mapper = new ObjectMapper();

		this.id = id;
		this.taskKey = taskKey;
		inputPipeName = "/tmp/input-" + UUID.randomUUID();
		outputPipeName = "/tmp/output-" + UUID.randomUUID();

		try {
			setup();
		} catch (Exception e) {
			cleanup();
			throw e;
		}
	}

	private void setup() throws IOException, InterruptedException {
		script = getClass().getResource("/" + taskKey + ".py").getPath();

		log.info("Creating input and output pipes: {} {} for task {}", inputPipeName, outputPipeName, id);

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

		log.info("Writing request payload to input pipe: {} for task: {}", inputPipeName, id);

		processBuilder = new ProcessBuilder("python", script, "--input_pipe", inputPipeName,
				"--output_pipe", outputPipeName);
	}

	public void writeInputWithTimeout(byte[] bytes, int timeoutMinutes) throws IOException, InterruptedException {
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
			try {
				// Write to the named pipe in a separate thread
				try (FileOutputStream fos = new FileOutputStream(inputPipeName)) {
					fos.write(appendNewline(bytes));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		});

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException("Error while writing to pipe", e);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new RuntimeException("Writing to pipe took too long", e);
		}

		if (result == null) {
			return;
		}
		if (result instanceof Integer) {
			// process has exited early
			if (status == TaskStatus.CANCELLED) {
				throw new InterruptedException("Process for task " + id + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + id + " exited early with code " + result);
		}
		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	public byte[] readOutputWithTimeout(int timeoutMinutes)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(outputPipeName)))) {
				return removeNewline(reader.readLine().getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		Object result;
		try {
			result = CompletableFuture.anyOf(future, processFuture).get(timeoutMinutes, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new RuntimeException("Reading from pipe took too long", e);
		}

		if (result == null) {
			throw new RuntimeException("Unexpected null result");
		}

		if (result instanceof byte[]) {
			// we got our response
			return (byte[]) result;
		}
		if (result instanceof Integer) {
			// process has exited early
			if (status == TaskStatus.CANCELLED) {
				throw new InterruptedException("Process for task " + id + " has been cancelled");
			}
			throw new InterruptedException("Process for task " + id + " exited early with code " + result);
		}

		throw new RuntimeException("Unexpected result type: " + result.getClass());
	}

	private byte[] appendNewline(byte[] original) {
		byte[] newline = System.lineSeparator().getBytes();
		byte[] combined = new byte[original.length + newline.length];

		System.arraycopy(original, 0, combined, 0, original.length);
		System.arraycopy(newline, 0, combined, original.length, newline.length);

		return combined;
	}

	private byte[] removeNewline(byte[] original) {
		int newlineLength = System.lineSeparator().getBytes().length;
		if (original.length < newlineLength) {
			return original;
		}

		byte[] trimmed = new byte[original.length - newlineLength];
		System.arraycopy(original, 0, trimmed, 0, trimmed.length);

		return trimmed;
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

		lock.lock(() -> {
			if (status == TaskStatus.CANCELLED) {
				// don't run if we already cancelled
				throw new InterruptedException("Task has already been cancelled");
			}

			if (status != TaskStatus.QUEUED) {
				// has to be in a queued state to be valid to run
				throw new RuntimeException("Task has already been started");
			}

			status = TaskStatus.RUNNING;
		});

		process = processBuilder.start();

		// Create a future to signal when the process has exited
		processFuture = CompletableFuture.supplyAsync(() -> {
			try {
				int exitCode = process.waitFor();
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
				log.info("Process exited with code " + exitCode);
				return exitCode;
			} catch (InterruptedException e) {
				log.warn("Process failed to exit cleanly " + e);
				lock.lock(() -> {
					status = TaskStatus.FAILED;
				});
			}
			return 1;
		});

		InputStream inputStream = process.getInputStream();
		InputStream errorStream = process.getErrorStream();

		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					log.info("[{}] stdout: {}", id, line);
				}
			} catch (IOException e) {
				log.warn("Error occured while logging stdout for task {}", id);
			}
		}).start();

		new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					log.info("[{}] stderr: {}", id, line);
				}
			} catch (IOException e) {
				log.warn("Error occured while logging stderr for task {}", id);
			}
		}).start();
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

	public boolean cancel() {
		return lock.lock(() -> {
			if (status == TaskStatus.QUEUED) {
				// if we havaen't started yet, flag it as cancelled
				status = TaskStatus.CANCELLED;
				return false;
			}
			if (status != TaskStatus.RUNNING) {
				// can't cancel a process if it isn't in a running state
				return false;
			}

			status = TaskStatus.CANCELLING;

			if (process == null) {
				throw new RuntimeException("Process has not been started");
			}

			// Kill the process forcibly (SIGKILL)
			process.destroyForcibly();
			return true;
		});
	}

	TaskStatus getStatus() {
		return lock.lock(() -> {
			return status;
		});
	}

}
