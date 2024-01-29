package software.uncharted.terarium.taskrunner.service;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;

@Slf4j
public class TaskRunnerServiceTests extends TaskRunnerApplicationTests {
	@Autowired
	TaskRunnerService taskRunnerService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	ObjectMapper mapper = new ObjectMapper();

	private final int REPEAT_COUNT = 1;
	private final long TIMEOUT_SECONDS = 30;
	private final String TEST_INPUT = "{\"research_paper\":\"Test research paper\"}";
	private final String FAILURE_INPUT = "{\"should_fail\":true}";
	private final String SCRIPT_PATH = getClass().getResource("/echo.py").getPath();

	@BeforeEach
	public void setup() {
		taskRunnerService.destroyQueues();
		taskRunnerService.declareQueues();
		taskRunnerService.declareAndBindTransientQueueWithRoutingKey(
				taskRunnerService.TASK_RUNNER_RESPONSE_EXCHANGE,
				taskRunnerService.TASK_RUNNER_RESPONSE_QUEUE, "");
	}

	@AfterEach
	public void teardown() {
		taskRunnerService.destroyQueues();
	}

	private List<TaskResponse> consumeAllResponses() throws InterruptedException {
		BlockingQueue<TaskResponse> queue = consumeForResponses();
		List<TaskResponse> responses = new ArrayList<>();

		while (true) {
			try {
				TaskResponse resp = queue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				responses.add(resp);
				if (resp.getStatus() == TaskStatus.SUCCESS ||
						resp.getStatus() == TaskStatus.FAILED ||
						resp.getStatus() == TaskStatus.CANCELLED) {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		return responses;
	}

	private BlockingQueue<TaskResponse> consumeForResponses() {

		BlockingQueue<TaskResponse> queue = new ArrayBlockingQueue<>(10);

		CompletableFuture<Void> processFuture = new CompletableFuture<>();

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitTemplate.getConnectionFactory());
		container.setQueueNames(taskRunnerService.TASK_RUNNER_RESPONSE_QUEUE);
		container.setMessageListener(message -> {
			log.info("DID WE GET SOMETHING?");
			try {
				TaskResponse resp = mapper.readValue(message.getBody(), TaskResponse.class);
				queue.put(resp);

				if (resp.getStatus() == TaskStatus.SUCCESS ||
						resp.getStatus() == TaskStatus.FAILED ||
						resp.getStatus() == TaskStatus.CANCELLED) {
					// signal we are done
					processFuture.complete(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				processFuture.complete(null);
			}
		});

		container.start();

		new Thread(() -> {
			try {
				int TIMEOUT_SECONDS = 10;
				processFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			} finally {
				container.stop();
			}
		}).start();

		return queue;
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskSuccess() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		List<TaskResponse> responses = consumeAllResponses();

		Assertions.assertTrue(responses.size() == 2);
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(1).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskFailure() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(FAILURE_INPUT).getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		List<TaskResponse> responses = consumeAllResponses();

		Assertions.assertTrue(responses.size() == 2);
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.FAILED, responses.get(1).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskCancelled() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		BlockingQueue<TaskResponse> queue = consumeForResponses();
		List<TaskResponse> responses = new ArrayList<>();

		while (true) {
			TaskResponse resp = queue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			responses.add(resp);
			if (resp.getStatus() == TaskStatus.RUNNING) {
				// send the cancellation after we know the task has started
				rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
						req.getId().toString(),
						"");
			}
			if (resp.getStatus() == TaskStatus.CANCELLED) {
				break;
			}
		}

		Assertions.assertTrue(responses.size() == 3);
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.CANCELLING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.CANCELLED, responses.get(2).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskCancelledBeforeStart() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setScript(SCRIPT_PATH);
		req.setInput(new String(TEST_INPUT).getBytes());
		req.setTimeoutMinutes(1);

		// we have to create this queue before sending the cancellation to know that
		// there is a queue to get the msg
		String cancelQueue = req.getId().toString();
		String routingKey = req.getId().toString();
		taskRunnerService.declareAndBindTransientQueueWithRoutingKey(
				taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE, cancelQueue,
				routingKey);

		// send the cancellation BEFORE we send the request, this simulates a taskrunner
		// under
		// contention that could receive a cancellation before it processes a request
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
				req.getId().toString(),
				"");

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		List<TaskResponse> responses = consumeAllResponses();

		Assertions.assertTrue(responses.size() == 1);
		Assertions.assertEquals(TaskStatus.CANCELLED, responses.get(0).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskSoakTest()
			throws InterruptedException, JsonProcessingException, ExecutionException, TimeoutException {

		int NUM_REQUESTS = 64;
		int NUM_THREADS = 4;

		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

		ConcurrentHashMap<UUID, List<TaskResponse>> responsesPerReq = new ConcurrentHashMap<>();
		ConcurrentHashMap<UUID, List<List<TaskStatus>>> expectedResponses = new ConcurrentHashMap<>();

		List<Future<?>> requestFutures = new ArrayList<>();
		ConcurrentHashMap<UUID, CompletableFuture<Void>> responseFutures = new ConcurrentHashMap<>();

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitTemplate.getConnectionFactory());
		container.setQueueNames(taskRunnerService.TASK_RUNNER_RESPONSE_QUEUE);
		container.setMessageListener(message -> {
			try {
				TaskResponse resp = mapper.readValue(message.getBody(), TaskResponse.class);
				responsesPerReq.get(resp.getId()).add(resp);

				if (resp.getStatus() == TaskStatus.SUCCESS || resp.getStatus() == TaskStatus.CANCELLED
						|| resp.getStatus() == TaskStatus.FAILED) {
					responseFutures.get(resp.getId()).complete(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		container.start();

		Random rand = new Random();

		for (int i = 0; i < NUM_REQUESTS; i++) {

			Future<?> future = executor.submit(() -> {
				try {
					TaskRequest req = new TaskRequest();
					req.setId(UUID.randomUUID());
					req.setScript(SCRIPT_PATH);
					req.setTimeoutMinutes(1);

					// allocate the response stuff
					responsesPerReq.put(req.getId(), Collections.synchronizedList(new ArrayList<>()));
					responseFutures.put(req.getId(), new CompletableFuture<>());

					// we have to create this queue before sending the cancellation to know that
					// there is a queue to get the msg
					String cancelQueue = req.getId().toString();
					String routingKey = req.getId().toString();
					taskRunnerService.declareAndBindTransientQueueWithRoutingKey(
							taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE, cancelQueue,
							routingKey);

					boolean shouldCancelBefore = false;
					boolean shouldCancelAfter = false;

					int randomNumber = rand.nextInt(4);
					switch (randomNumber) {
						case 0:
							// success
							req.setInput(new String(TEST_INPUT).getBytes());
							expectedResponses.put(req.getId(),
									List.of(List.of(TaskStatus.RUNNING, TaskStatus.SUCCESS)));
							break;
						case 1:
							// failure
							req.setInput(new String(FAILURE_INPUT).getBytes());
							expectedResponses.put(req.getId(), List.of(List.of(TaskStatus.RUNNING, TaskStatus.FAILED)));
							break;
						case 2:
							// cancellation
							req.setInput(new String(TEST_INPUT).getBytes());
							shouldCancelBefore = true;
							expectedResponses.put(req.getId(), List.of(
									List.of(TaskStatus.CANCELLED) // cancelled before request processed
							));
							break;
						case 3:
							// cancellation
							req.setInput(new String(TEST_INPUT).getBytes());
							shouldCancelAfter = true;
							expectedResponses.put(req.getId(), List.of(
									List.of(TaskStatus.CANCELLED), // cancelled before request processed
									List.of(TaskStatus.RUNNING, TaskStatus.CANCELLING, TaskStatus.CANCELLED), // cancelled
																												// during
																												// processing
									List.of(TaskStatus.RUNNING, TaskStatus.SUCCESS) // cancelled after processing
							));
							break;
						default:
							throw new RuntimeException("This shouldnt happen");
					}

					if (shouldCancelBefore) {
						// send the cancellation before we send the request
						rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
								req.getId().toString(),
								"");
					}

					// send the request
					String reqStr = mapper.writeValueAsString(req);
					rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

					if (shouldCancelAfter) {
						Thread.sleep(1000);
						// send the cancellation
						rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
								req.getId().toString(),
								"");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			requestFutures.add(future);
		}

		// wait for all the responses to be send
		for (Future<?> future : requestFutures) {
			future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}

		// wait for all the tasks to complete
		for (Future<?> future : responseFutures.values()) {
			future.get(TIMEOUT_SECONDS * 10, TimeUnit.SECONDS);
		}

		container.stop();

		// check that the responses are valid
		for (Map.Entry<UUID, List<TaskResponse>> responseEntry : responsesPerReq.entrySet()) {

			UUID id = responseEntry.getKey();
			List<TaskResponse> responses = responseEntry.getValue();

			List<List<TaskStatus>> possibleExpected = expectedResponses.get(id);

			boolean found = false;
			for (List<TaskStatus> expected : possibleExpected) {
				if (expected.size() != responses.size()) {
					continue;
				}
				for (int i = 0; i < expected.size(); i++) {
					if (expected.get(i) != responses.get(i).getStatus()) {
						if (responses.get(i).getOutput() != null) {
							Assertions.assertArrayEquals("{\"result\":\"ok\"}".getBytes(),
									responses.get(i).getOutput());
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
