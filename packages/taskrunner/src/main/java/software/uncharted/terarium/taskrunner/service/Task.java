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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Task {

	private UUID id;
	private String taskKey;
	private ObjectMapper mapper;
	private ProcessBuilder processBuilder;
	private Process process;
	private String inputPipeName;
	private String outputPipeName;

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

	public void writeInput(byte[] bytes) throws IOException {
		// Write to the named pipe in a separate thread
		try (FileOutputStream fos = new FileOutputStream(inputPipeName)) {
			fos.write(appendNewline(bytes));
		}
	}

	public void writeInputWithTimeout(byte[] bytes, int timeoutMinutes) throws IOException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = executor.submit(() -> {
			try {
				// Write to the named pipe in a separate thread
				try (FileOutputStream fos = new FileOutputStream(inputPipeName)) {
					fos.write(appendNewline(bytes));
				}
				log.info("WROTE THE INPUT!");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		try {
			future.get(timeoutMinutes, TimeUnit.MINUTES); // Timeout of 5 minutes
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new RuntimeException("Writing to pipe took too long", e);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException("Error while writing to pipe", e);
		} finally {
			executor.shutdownNow();
		}
	}

	public byte[] readOutput() throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(outputPipeName)))) {
			return removeNewline(reader.readLine().getBytes());
		}
	}

	public byte[] readOutputWithTimeout(int timeoutMinutes)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<byte[]> future = executor.submit(() -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(outputPipeName)))) {
				return removeNewline(reader.readLine().getBytes());
			}
		});

		byte[] result;
		try {
			result = future.get(timeoutMinutes, TimeUnit.MINUTES); // set timeout
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new RuntimeException("Reading from pipe took too long", e);
		} finally {
			executor.shutdownNow(); // always stop the executor
		}

		return result;
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

	public void start() throws IOException {
		process = processBuilder.start();
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

	public void waitFor(int timeoutMinutes) throws InterruptedException, TimeoutException {
		boolean hasExited = process.waitFor((long) timeoutMinutes, TimeUnit.MINUTES);
		if (hasExited) {
			int exitCode = process.exitValue();
			if (exitCode != 0) {
				throw new RuntimeException("Python script exited with non-zero exit code: " + exitCode);
			}
		} else {
			throw new TimeoutException("Process did not exit within the timeout");
		}
	}

	public void cancel() {
		if (process != null) {
			// Kill the process forcibly (SIGKILL)
			process.destroyForcibly();
		}
	}

}
