package software.uncharted.terarium.taskrunner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;
import software.uncharted.terarium.taskrunner.models.task.ProgressState;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;

@Slf4j
public class TaskRunnerServiceTests extends TaskRunnerApplicationTests {

	@Autowired
	TaskRunnerService taskRunnerService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	ObjectMapper mapper = new ObjectMapper();

	private final long TIMEOUT_SECONDS = 30;
	private final String TEST_INPUT = "{\"document\":\"Test research paper\"}";
	private final String TEST_INPUT_WITH_PROGRESS = "{\"document\":\"Test research paper\",\"include_progress\":true}";
	private final String FAILURE_INPUT = "{\"should_fail\":true}";
	private final String SCRIPT_PATH = getClass().getResource("/echo.py").getPath();
	private final String TASK_RUNNER_RESPONSE_QUEUE = "terarium-response-queue-test";

	String responseRoutingKey = "test-routing-key";

	@BeforeEach
	public void setup() {
		taskRunnerService.destroyQueues();
		taskRunnerService.declareQueues();
		taskRunnerService.declareAndBindTransientQueueWithRoutingKey(
			taskRunnerService.TASK_RUNNER_RESPONSE_EXCHANGE,
			TASK_RUNNER_RESPONSE_QUEUE,
			responseRoutingKey
		);
	}

	@AfterEach
	public void teardown() {
		taskRunnerService.destroyQueues();
	}

	private List<TaskResponse> consumeAllResponses() throws InterruptedException {
		final BlockingQueue<TaskResponse> queue = consumeForResponses();
		final List<TaskResponse> responses = new ArrayList<>();

		while (true) {
			try {
				final TaskResponse resp = queue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				responses.add(resp);
				if (
					resp.getStatus() == ProgressState.COMPLETE ||
					resp.getStatus() == ProgressState.ERROR ||
					resp.getStatus() == ProgressState.CANCELLED
				) {
					break;
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		return responses;
	}

	private BlockingQueue<TaskResponse> consumeForResponses() {
		final BlockingQueue<TaskResponse> queue = new ArrayBlockingQueue<>(10);

		final CompletableFuture<Void> processFuture = new CompletableFuture<>();

		final DirectMessageListenerContainer container = new DirectMessageListenerContainer(
			rabbitTemplate.getConnectionFactory()
		);
		container.setPrefetchCount(1);
		container.setQueueNames(TASK_RUNNER_RESPONSE_QUEUE);
		container.setMessageListener(message -> {
			try {
				final TaskResponse resp = mapper.readValue(message.getBody(), TaskResponse.class);
				queue.put(resp);

				if (
					resp.getStatus() == ProgressState.COMPLETE ||
					resp.getStatus() == ProgressState.ERROR ||
					resp.getStatus() == ProgressState.CANCELLED
				) {
					// signal we are done
					processFuture.complete(null);
				}
			} catch (final Exception e) {
				e.printStackTrace();
				processFuture.complete(null);
			}
		});

		container.start();

		new Thread(() -> {
			try {
				final int TIMEOUT_SECONDS = 10;
				processFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			} finally {
				container.stop();
			}
		}).start();

		return queue;
	}

	@Test
	public void testRunTaskSuccess() throws InterruptedException, JsonProcessingException {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(TEST_INPUT_WITH_PROGRESS.getBytes());
		req.setTimeoutMinutes(1);
		req.setRoutingKey(responseRoutingKey);

		final String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		final List<TaskResponse> responses = consumeAllResponses();

		Assertions.assertEquals(7, responses.size());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(2).getStatus());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(3).getStatus());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(4).getStatus());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(5).getStatus());
		Assertions.assertEquals(ProgressState.COMPLETE, responses.get(6).getStatus());
	}

	@Test
	public void testRunTaskFailure() throws InterruptedException, JsonProcessingException {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(FAILURE_INPUT.getBytes());
		req.setTimeoutMinutes(1);
		req.setRoutingKey(responseRoutingKey);

		final String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		final List<TaskResponse> responses = consumeAllResponses();

		Assertions.assertEquals(2, responses.size());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(ProgressState.ERROR, responses.get(1).getStatus());
	}

	@Test
	public void testRunTaskCancelled() throws InterruptedException, JsonProcessingException {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(TEST_INPUT_WITH_PROGRESS.getBytes());
		req.setTimeoutMinutes(1);
		req.setRoutingKey(responseRoutingKey);

		final String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		final BlockingQueue<TaskResponse> queue = consumeForResponses();
		final List<TaskResponse> responses = new ArrayList<>();

		while (true) {
			final TaskResponse resp = queue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			responses.add(resp);
			if (resp.getStatus() == ProgressState.RUNNING) {
				// send the cancellation after we know the task has started
				rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE, req.getId().toString(), "");
			}
			if (resp.getStatus() == ProgressState.CANCELLED) {
				break;
			}
		}

		Assertions.assertEquals(2, responses.size());
		Assertions.assertEquals(ProgressState.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(ProgressState.CANCELLED, responses.get(2).getStatus());
	}

	@Test
	public void testRunTaskCancelledBeforeStart() throws InterruptedException, JsonProcessingException {
		final TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(TEST_INPUT_WITH_PROGRESS.getBytes());
		req.setTimeoutMinutes(1);
		req.setRoutingKey(responseRoutingKey);

		// we have to create this queue before sending the cancellation to know that
		// there is a queue to get the msg
		final String cancelQueue = req.getId().toString();
		final String routingKey = req.getId().toString();
		taskRunnerService.declareAndBindTransientQueueWithRoutingKey(
			taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
			cancelQueue,
			routingKey
		);

		// send the cancellation BEFORE we send the request, this simulates a taskrunner
		// under
		// contention that could receive a cancellation before it processes a request
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE, req.getId().toString(), "");

		final String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		final List<TaskResponse> responses = consumeAllResponses();

		Assertions.assertEquals(1, responses.size());
		Assertions.assertEquals(ProgressState.CANCELLED, responses.get(0).getStatus());
	}

	@Test
	public void testRunTaskSoakTest()
		throws InterruptedException, JsonProcessingException, ExecutionException, TimeoutException {
		final int NUM_REQUESTS = 64;
		final int NUM_THREADS = 4;

		final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

		final ConcurrentHashMap<UUID, List<TaskResponse>> responsesPerReq = new ConcurrentHashMap<>();
		final ConcurrentHashMap<UUID, List<List<ProgressState>>> expectedResponses = new ConcurrentHashMap<>();

		final List<Future<?>> requestFutures = new ArrayList<>();
		final ConcurrentHashMap<UUID, CompletableFuture<Void>> responseFutures = new ConcurrentHashMap<>();

		final DirectMessageListenerContainer container = new DirectMessageListenerContainer(
			rabbitTemplate.getConnectionFactory()
		);
		container.setPrefetchCount(1);
		container.setQueueNames(TASK_RUNNER_RESPONSE_QUEUE);
		container.setMessageListener(message -> {
			try {
				final TaskResponse resp = mapper.readValue(message.getBody(), TaskResponse.class);
				responsesPerReq.get(resp.getId()).add(resp);

				if (
					resp.getStatus() == ProgressState.COMPLETE ||
					resp.getStatus() == ProgressState.CANCELLED ||
					resp.getStatus() == ProgressState.ERROR
				) {
					responseFutures.get(resp.getId()).complete(null);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		container.start();

		final Random rand = new Random();

		for (int i = 0; i < NUM_REQUESTS; i++) {
			final Future<?> future = executor.submit(() -> {
				try {
					final TaskRequest req = new TaskRequest();
					req.setId(UUID.randomUUID());
					req.setScript(SCRIPT_PATH);
					req.setTimeoutMinutes(1);
					req.setRoutingKey(responseRoutingKey);

					// allocate the response stuff
					responsesPerReq.put(req.getId(), Collections.synchronizedList(new ArrayList<>()));
					responseFutures.put(req.getId(), new CompletableFuture<>());

					// we have to create this queue before sending the cancellation to know that
					// there is a queue to get the msg
					final String cancelQueue = req.getId().toString();
					final String routingKey = req.getId().toString();
					taskRunnerService.declareAndBindTransientQueueWithRoutingKey(
						taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
						cancelQueue,
						routingKey
					);

					boolean shouldCancelBefore = false;
					boolean shouldCancelAfter = false;

					final int randomNumber = rand.nextInt(4);
					switch (randomNumber) {
						case 0:
							// success
							req.setInput(TEST_INPUT.getBytes());
							expectedResponses.put(req.getId(), List.of(List.of(ProgressState.RUNNING, ProgressState.COMPLETE)));
							break;
						case 1:
							// failure
							req.setInput(FAILURE_INPUT.getBytes());
							expectedResponses.put(req.getId(), List.of(List.of(ProgressState.RUNNING, ProgressState.ERROR)));
							break;
						case 2:
							// cancellation
							req.setInput(TEST_INPUT.getBytes());
							shouldCancelBefore = true;
							expectedResponses.put(
								req.getId(),
								List.of(
									List.of(ProgressState.CANCELLED) // cancelled before request processed
								)
							);
							break;
						case 3:
							// cancellation
							req.setInput(TEST_INPUT.getBytes());
							shouldCancelAfter = true;
							expectedResponses.put(
								req.getId(),
								List.of(
									List.of(ProgressState.CANCELLED), // cancelled before request processed
									List.of(ProgressState.RUNNING, ProgressState.CANCELLED), // cancelled
									// during
									// processing
									List.of(ProgressState.RUNNING, ProgressState.COMPLETE) // cancelled after processing
								)
							);
							break;
						default:
							throw new RuntimeException("This shouldnt happen");
					}

					if (shouldCancelBefore) {
						// send the cancellation before we send the request
						rabbitTemplate.convertAndSend(
							taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
							req.getId().toString(),
							""
						);
					}

					// send the request
					final String reqStr = mapper.writeValueAsString(req);
					rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

					if (shouldCancelAfter) {
						Thread.sleep(1000);
						// send the cancellation
						rabbitTemplate.convertAndSend(
							taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
							req.getId().toString(),
							""
						);
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			});

			requestFutures.add(future);
		}

		// wait for all the responses to be send
		for (final Future<?> future : requestFutures) {
			future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}

		// wait for all the tasks to complete
		for (final Future<?> future : responseFutures.values()) {
			future.get(TIMEOUT_SECONDS * 10, TimeUnit.SECONDS);
		}

		container.stop();

		// check that the responses are valid
		for (final Map.Entry<UUID, List<TaskResponse>> responseEntry : responsesPerReq.entrySet()) {
			final UUID id = responseEntry.getKey();
			final List<TaskResponse> responses = responseEntry.getValue();

			final List<List<ProgressState>> possibleExpected = expectedResponses.get(id);

			boolean found = false;
			for (final List<ProgressState> expected : possibleExpected) {
				if (expected.size() != responses.size()) {
					continue;
				}
				for (int i = 0; i < expected.size(); i++) {
					if (expected.get(i) != responses.get(i).getStatus()) {
						if (responses.get(i).getOutput() != null) {
							Assertions.assertArrayEquals("{\"result\":\"ok\"}".getBytes(), responses.get(i).getOutput());
						}
						break;
					}
				}
				found = true;
				break;
			}

			Assertions.assertTrue(found);
		}
	}
}
