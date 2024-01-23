package software.uncharted.terarium.taskrunner.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

	public Task(UUID id, String taskKey) {
		mapper = new ObjectMapper();

		this.id = id;
		this.taskKey = taskKey;
		inputPipeName = "/tmp/input-" + UUID.randomUUID();
		outputPipeName = "/tmp/output-" + UUID.randomUUID();
	}

	public void setup() throws IOException, InterruptedException {

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

	public byte[] readOutput() throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(outputPipeName)))) {
			return removeNewline(reader.readLine().getBytes());
		}
	}

	public byte[] readOutput(int timeoutSeconds)
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
			result = future.get(timeoutSeconds, TimeUnit.SECONDS); // set timeout
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

	public void teardown() {
		try {
			Files.deleteIfExists(Paths.get(inputPipeName));
			Files.deleteIfExists(Paths.get(outputPipeName));
		} catch (Exception e) {
			log.warn("Exception occurred while cleaning up the task pipes:" + e);
		}
	}

	public void run() throws IOException, InterruptedException {
		process = processBuilder.start();
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Python script exited with non-zero exit code: " + exitCode);
		}
	}

	public void cancel() {
		if (process != null) {
			// Kill the process forcibly (SIGKILL)
			process.destroyForcibly();
		}
	}

}
