package software.uncharted.terarium.taskrunner.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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

	@AfterEach
	public void cleanup() {
		RabbitAdmin admin = new RabbitAdmin(rabbitTemplate);

		admin.deleteQueue(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE);
		admin.deleteQueue(taskRunnerService.TASK_RUNNER_RESPONSE_QUEUE);
		admin.deleteQueue(taskRunnerService.TASK_RUNNER_CANCELLATION_EXCHANGE);
	}

	@Test
	public void testRunTask() throws InterruptedException, JsonProcessingException {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"research_paper\": \"Test research paper\"}").getBytes());
		req.setTimeoutMinutes(1);

		String reqStr = mapper.writeValueAsString(req);
		rabbitTemplate.convertAndSend(taskRunnerService.TASK_RUNNER_REQUEST_QUEUE, reqStr);

		AtomicBoolean isFinished = new AtomicBoolean(false);
		List<TaskResponse> responses = new ArrayList<>();

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitTemplate.getConnectionFactory());
		container.setQueueNames(taskRunnerService.TASK_RUNNER_RESPONSE_QUEUE);
		container.setMessageListener(message -> {
			try {
				TaskResponse resp = mapper.readValue(message.getBody(), TaskResponse.class);
				responses.add(resp);

				if (resp.getStatus() == TaskStatus.SUCCESS || resp.getStatus() == TaskStatus.FAILED) {
					isFinished.set(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				isFinished.set(true);
			}
		});
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
		Assertions.assertEquals(responses.get(0).getStatus(), TaskStatus.RUNNING);
		Assertions.assertEquals(responses.get(1).getStatus(), TaskStatus.SUCCESS);
	}
}
