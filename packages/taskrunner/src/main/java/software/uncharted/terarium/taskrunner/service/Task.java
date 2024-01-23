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
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;

@Data
@Slf4j
public class Task {

	private ObjectMapper mapper;
	private TaskRequest req;
	private ProcessBuilder processBuilder;
	private Process process;

	private String script;

	public Task(TaskRequest req) {
		mapper = new ObjectMapper();

		this.req = req;
		if (this.req.getId() == null) {
			this.req.setId(UUID.randomUUID());
		}
		this.req.setInputPipe("/tmp/input-" + UUID.randomUUID());
		this.req.setOutputPipe("/tmp/output-" + UUID.randomUUID());
	}

	public byte[] getRequestBytes() throws IOException {
		return mapper.writeValueAsBytes(req);
	}

	public void setup() throws IOException, InterruptedException {

		script = getClass().getResource("/" + req.getTaskKey() + ".py").getPath();

		log.info("Creating input and output pipes: {} {} for task {}", req.getInputPipe(), req.getOutputPipe(),
				req.getId());

		// Create the named pipes
		Process inputPipe = new ProcessBuilder("mkfifo", req.getInputPipe()).start();
		int exitCode = inputPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}

		Process outputPipe = new ProcessBuilder("mkfifo", req.getOutputPipe()).start();
		exitCode = outputPipe.waitFor();
		if (exitCode != 0) {
			throw new RuntimeException("Error creating input pipe");
		}

		log.info("Writing request payload to input pipe: {} for task: {}", req.getInputPipe(), req.getId());

		processBuilder = new ProcessBuilder("python", script, "--input_pipe", req.getInputPipe(),
				"--output_pipe", req.getOutputPipe());
	}

	public void writeInput(byte[] bytes) throws IOException {
		// Write to the named pipe in a separate thread
		try (FileOutputStream fos = new FileOutputStream(req.getInputPipe())) {
			fos.write(appendNewline(bytes));
		}
	}

	public byte[] readOutput() throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(req.getOutputPipe())))) {
			return removeNewline(reader.readLine().getBytes());
		}
	}

	public byte[] readOutput(int timeoutSeconds)
			throws IOException, InterruptedException, ExecutionException, TimeoutException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<byte[]> future = executor.submit(() -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(req.getOutputPipe())))) {
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
			Files.deleteIfExists(Paths.get(req.getInputPipe()));
			Files.deleteIfExists(Paths.get(req.getOutputPipe()));
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
