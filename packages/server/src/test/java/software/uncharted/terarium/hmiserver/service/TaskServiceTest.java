package software.uncharted.terarium.hmiserver.service;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

public class TaskServiceTest extends TerariumApplicationTests {

	@Autowired
	private TaskService taskService;

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateEchoTaskRequest() throws Exception {

		UUID taskId = UUID.randomUUID();
		String additionalProps = "These are additional properties";

		byte[] input = "{\"input\":\"This is my input string\"}".getBytes();

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript("/echo.py");
		req.setInput(input);
		req.setAdditionalProperties(additionalProps);

		List<TaskResponse> responses = taskService.runTaskBlocking(req);

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());

		for (TaskResponse resp : responses) {
			Assertions.assertEquals(taskId, resp.getId());
			Assertions.assertEquals(additionalProps, resp.getAdditionalProperties(String.class));
		}
	}

}
