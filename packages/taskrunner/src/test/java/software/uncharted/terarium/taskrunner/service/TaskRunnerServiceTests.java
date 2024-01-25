package software.uncharted.terarium.taskrunner.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;

public class TaskRunnerServiceTests extends TaskRunnerApplicationTests {
	@Autowired
	TaskRunnerService taskRunnerService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	ObjectMapper mapper = new ObjectMapper();

	private final int REPEAT_COUNT = 1;

	@BeforeEach
	public void setup() {
		taskRunnerService.declareQueues();
	}

	@AfterEach
	public void teardown() {
		taskRunnerService.destroyQueues();
	}

	private SimpleMessageListenerContainer getResponseConsumer(AtomicBoolean isFinished, List<TaskResponse> responses) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitTemplate.getConnectionFactory());
		container.setQueueNames(taskRunnerService.TASK_RUNNER_RESPONSE_QUEUE);
		container.setMessageListener(message -> {
			try {
				TaskResponse resp = mapper.readValue(message.getBody(), TaskResponse.class);
				responses.add(resp);

				if (resp.getStatus() == TaskStatus.SUCCESS || resp.getStatus() == TaskStatus.FAILED
						|| resp.getStatus() == TaskStatus.CANCELLED) {
					isFinished.set(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				isFinished.set(true);
			}
		});
		return container;
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskSuccess() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"research_paper\": \"Test research paper\"}").getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		AtomicBoolean isFinished = new AtomicBoolean(false);
		List<TaskResponse> responses = Collections.synchronizedList(new ArrayList<>());

		SimpleMessageListenerContainer container = getResponseConsumer(isFinished, responses);
		container.start();

		int MAX_WAIT = 10000;
		long start = System.currentTimeMillis();
		while (!isFinished.get()) {
			Thread.sleep(100);

			if (System.currentTimeMillis() - start > MAX_WAIT) {
				break;
			}
		}

		container.stop();

		Assertions.assertTrue(responses.size() == 2);
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(1).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskFailure() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"should_fail\": true}").getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		AtomicBoolean isFinished = new AtomicBoolean(false);
		List<TaskResponse> responses = Collections.synchronizedList(new ArrayList<>());

		SimpleMessageListenerContainer container = getResponseConsumer(isFinished, responses);
		container.start();

		int MAX_WAIT = 10000;
		long start = System.currentTimeMillis();
		while (!isFinished.get()) {
			Thread.sleep(100);

			if (System.currentTimeMillis() - start > MAX_WAIT) {
				break;
			}
		}

		container.stop();

		Assertions.assertTrue(responses.size() == 2);
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.FAILED, responses.get(1).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskCancelled() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"research_paper\": \"Test research paper\"}").getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		AtomicBoolean isFinished = new AtomicBoolean(false);
		List<TaskResponse> responses = Collections.synchronizedList(new ArrayList<>());

		SimpleMessageListenerContainer container = getResponseConsumer(isFinished, responses);
		container.start();

		int MAX_WAIT = 10000;
		long start = System.currentTimeMillis();
		while (!isFinished.get()) {
			Thread.sleep(100);

			if (responses.size() > 0) {
				// send the cancellation after we know the task has started
				rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE,
						req.getId().toString(),
						"");
			}

			if (System.currentTimeMillis() - start > MAX_WAIT) {
				break;
			}
		}

		container.stop();

		Assertions.assertTrue(responses.size() == 3);
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.CANCELLING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.CANCELLED, responses.get(2).getStatus());
	}

	@RepeatedTest(REPEAT_COUNT)
	public void testRunTaskCancelledBeforeStart() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"research_paper\": \"Test research paper\"}").getBytes());
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

		AtomicBoolean isFinished = new AtomicBoolean(false);
		List<TaskResponse> responses = Collections.synchronizedList(new ArrayList<>());

		SimpleMessageListenerContainer container = getResponseConsumer(isFinished, responses);
		container.start();

		int MAX_WAIT = 10000;
		long start = System.currentTimeMillis();
		while (!isFinished.get()) {
			Thread.sleep(100);

			if (System.currentTimeMillis() - start > MAX_WAIT) {
				break;
			}
		}

		container.stop();

		Assertions.assertTrue(responses.size() == 1);
		Assertions.assertEquals(TaskStatus.CANCELLED, responses.get(0).getStatus());
	}
}
