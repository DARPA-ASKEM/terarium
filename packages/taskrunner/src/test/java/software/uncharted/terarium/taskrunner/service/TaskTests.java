package software.uncharted.terarium.taskrunner.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;

@Slf4j
public class TaskTests extends TaskRunnerApplicationTests {

	private final String TEST_INPUT = "{\"document\":\"Test research paper\"}";
	private final String TEST_INPUT_WITH_PROGRESS = "{\"document\":\"Test research paper\",\"include_progress\":true}";
	private final String FAILURE_INPUT = "{\"should_fail\":true}";
	private final String SCRIPT_PATH = getClass().getResource("/echo.py").getPath();

	@Test
	public void testTaskSuccess() throws Exception {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());

		final int ONE_MINUTE = 1;

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.start();

			Assertions.assertEquals(TaskStatus.RUNNING, task.getStatus());
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);
			Assertions.assertArrayEquals(req.getInput(), output);

			task.waitFor(ONE_MINUTE);
			Assertions.assertEquals(TaskStatus.SUCCESS, task.getStatus());
		} catch (final Exception e) {
			throw e;
		} finally {
			task.cleanup();
		}
	}

	@Test
	public void testTaskSuccessWithProgress() throws Exception {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT_WITH_PROGRESS).getBytes());

		final int ONE_MINUTE = 1;

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.start();

			Assertions.assertEquals(TaskStatus.RUNNING, task.getStatus());
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			int progressCount = 0;
			while (true) {
				// block and wait for progress from the task
				final byte[] output = task.readProgressWithTimeout(ONE_MINUTE);
				if (output == null) {
					// no more progress
					break;
				}
				progressCount++;
			}

			Assertions.assertEquals(5, progressCount);

			final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);
			Assertions.assertArrayEquals(req.getInput(), output);

			task.waitFor(ONE_MINUTE);
			Assertions.assertEquals(TaskStatus.SUCCESS, task.getStatus());
		} catch (final Exception e) {
			throw e;
		} finally {
			task.cleanup();
		}
	}

	@Test
	public void testTaskLargeInputOutput() throws Exception {
		final ClassPathResource resource = new ClassPathResource("test_input.json");
		final String input = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(input.getBytes());

		final int ONE_MINUTE = 1;

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.start();

			Assertions.assertEquals(TaskStatus.RUNNING, task.getStatus());
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);

			Assertions.assertEquals(input, new String(output));
			Assertions.assertArrayEquals(input.getBytes(), output);

			task.waitFor(ONE_MINUTE);
			Assertions.assertEquals(TaskStatus.SUCCESS, task.getStatus());
		} catch (final Exception e) {
			throw e;
		} finally {
			task.cleanup();
		}
	}

	@Test
	public void testTaskFailure() throws Exception {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(FAILURE_INPUT).getBytes());

		final int ONE_MINUTE = 1;

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.start();

			Assertions.assertEquals(TaskStatus.RUNNING, task.getStatus());
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);
			Assertions.assertArrayEquals(req.getInput(), output);

			task.waitFor(ONE_MINUTE);
		} catch (final InterruptedException e) {
			// this should happen
		} finally {
			task.cleanup();
		}

		Assertions.assertEquals(TaskStatus.FAILED, task.getStatus());
	}

	@Test
	public void testTaskCancel() throws Exception {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());

		final int ONE_MINUTE = 1;

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.start();

			Assertions.assertEquals(TaskStatus.RUNNING, task.getStatus());
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			new Thread(() -> {
				try {
					Thread.sleep(1000);
					final boolean res = task.cancel();
					Assertions.assertTrue(res);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}).start();

			final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);
			Assertions.assertArrayEquals(req.getInput(), output);

			task.waitFor(ONE_MINUTE);
		} catch (final InterruptedException e) {
			// this should happen
		} finally {
			task.cleanup();
		}

		Assertions.assertEquals(TaskStatus.CANCELLED, task.getStatus());
	}

	@Test
	public void testTaskCancelMultipleTimes() throws Exception {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());

		final int ONE_MINUTE = 1;

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.start();

			Assertions.assertEquals(TaskStatus.RUNNING, task.getStatus());
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			new Thread(() -> {
				try {
					Thread.sleep(1000);
					boolean cancelled = false;
					for (int i = 0; i < 10; i++) {
						final boolean res = task.cancel();
						if (cancelled) {
							Assertions.assertFalse(res);
						} else {
							Assertions.assertTrue(res);
							cancelled = true;
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}).start();

			final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);
			Assertions.assertArrayEquals(req.getInput(), output);

			task.waitFor(ONE_MINUTE);
		} catch (final InterruptedException e) {
			// this should happen
		} finally {
			task.cleanup();
		}

		Assertions.assertEquals(TaskStatus.CANCELLED, task.getStatus());
	}

	@Test
	public void testTaskCancelBeforeStart() throws Exception {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());

		final Task task = new Task(req);
		try {
			Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());
			task.cancel();

			Assertions.assertEquals(TaskStatus.CANCELLED, task.getStatus());
			task.start();

			// we should not each this code
			Assertions.assertTrue(false);
		} catch (final InterruptedException e) {
			// this should happen
		} finally {
			task.cleanup();
		}

		Assertions.assertEquals(TaskStatus.CANCELLED, task.getStatus());
	}

	@Test
	public void testTaskSoakTest() throws Exception {
		final int NUM_REQUESTS = 64;
		final int NUM_THREADS = 8;
		final int ONE_MINUTE = 1;
		final int TIMEOUT_SECONDS = 60;

		final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

		final ConcurrentHashMap<UUID, TaskStatus> responses = new ConcurrentHashMap<>();
		final ConcurrentHashMap<UUID, TaskStatus> expectedResponses = new ConcurrentHashMap<>();
		final List<Future<?>> futures = new ArrayList<>();

		final Random rand = new Random();

		for (int i = 0; i < NUM_REQUESTS; i++) {
			Thread.sleep(500);
			final Future<?> future = executor.submit(() -> {
				try {
					final TaskRequest req = new TaskRequest();
					req.setId(UUID.randomUUID());
					req.setScript(SCRIPT_PATH);
					req.setTimeoutMinutes(1);

					boolean shouldCancelBefore = false;
					boolean shouldCancelAfter = false;
					TaskStatus expected = null;

					final int randomNumber = rand.nextInt(4);
					switch (randomNumber) {
						case 0:
							// success
							req.setInput(new String(TEST_INPUT).getBytes());
							expected = TaskStatus.SUCCESS;
							break;
						case 1:
							// failure
							req.setInput(new String(FAILURE_INPUT).getBytes());
							expected = TaskStatus.FAILED;
							break;
						case 2:
							// cancellation
							req.setInput(new String(TEST_INPUT).getBytes());
							expected = TaskStatus.CANCELLED;
							shouldCancelBefore = true;
							break;
						case 3:
							// cancellation
							req.setInput(new String(TEST_INPUT).getBytes());
							expected = TaskStatus.CANCELLED;
							shouldCancelAfter = true;
							break;
						default:
							throw new RuntimeException("This shouldnt happen");
					}
					expectedResponses.put(req.getId(), expected);

					final Task task = new Task(req);
					Assertions.assertEquals(TaskStatus.QUEUED, task.getStatus());

					if (shouldCancelBefore) {
						// dispatch the cancellation
						new Thread(
							new Runnable() {
								@Override
								public void run() {
									try {
										final boolean res = task.cancel();
										Assertions.assertTrue(res);
									} catch (final Exception e) {
										e.printStackTrace();
									}
								}
							}
						).start();
					}

					task.start();

					if (shouldCancelAfter) {
						// dispatch the cancellation
						new Thread(
							new Runnable() {
								@Override
								public void run() {
									try {
										Thread.sleep((long) (1000 * Math.random()));
										final boolean res = task.cancel();
										Assertions.assertTrue(res);
									} catch (final Exception e) {
										e.printStackTrace();
									}
								}
							}
						).start();
					}

					try {
						task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

						final byte[] output = task.readOutputWithTimeout(ONE_MINUTE);
						Assertions.assertArrayEquals(req.getInput(), output);

						task.waitFor(ONE_MINUTE);

						responses.put(req.getId(), TaskStatus.SUCCESS);
					} catch (TimeoutException | InterruptedException e) {
						if (expected == TaskStatus.CANCELLED) {
							responses.put(req.getId(), TaskStatus.CANCELLED);
						} else if (expected == TaskStatus.FAILED) {
							// this should happen
							responses.put(req.getId(), TaskStatus.FAILED);
						} else {
							throw e;
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			});

			futures.add(future);
		}

		// wait for all the responses to be send
		for (final Future<?> future : futures) {
			future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}

		// check that the responses are valid
		for (final Map.Entry<UUID, TaskStatus> response : responses.entrySet()) {
			final UUID id = response.getKey();
			final TaskStatus expected = expectedResponses.get(id);

			Assertions.assertEquals(expected, response.getValue());
		}
	}
}
