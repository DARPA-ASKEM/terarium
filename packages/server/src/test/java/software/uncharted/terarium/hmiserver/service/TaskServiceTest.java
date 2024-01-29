package software.uncharted.terarium.hmiserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

public class TaskServiceTest extends TerariumApplicationTests {

	@Autowired
	private TaskService taskService;

	@Autowired
	private ObjectMapper mapper;

	int POLL_TIMEOUT_SECONDS = 60;

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateEchoTaskRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		String jsonString = "{\"input\":\"This is my input string\"}";
		JsonNode jsonNode = mapper.readTree(jsonString);

		BlockingQueue<TaskResponse> responseQueue = taskService.createEchoTask(taskId, jsonNode);

		List<TaskResponse> responses = new ArrayList<>();
		while (true) {
			TaskResponse resp = responseQueue.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (resp == null) {
				break;
			}
			responses.add(resp);

			if (resp.getStatus() == TaskStatus.SUCCESS || resp.getStatus() == TaskStatus.FAILED
					|| resp.getStatus() == TaskStatus.CANCELLED) {
				break;
			}
		}

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());
	}

}
