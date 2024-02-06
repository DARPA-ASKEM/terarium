package software.uncharted.terarium.hmiserver.service;

import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

@Slf4j
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

	private String generateRandomString(int length) {
		String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder builder = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(characterSet.length());
			builder.append(characterSet.charAt(randomIndex));
		}

		return builder.toString();
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateLargeEchoTaskRequest() throws Exception {

		UUID taskId = UUID.randomUUID();
		String additionalProps = "These are additional properties";

		int STRING_LENGTH = 1048576;

		byte[] input = ("{\"input\":\"" + generateRandomString(STRING_LENGTH) + "\"}").getBytes();

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

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSendGoLLMModelCardRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		ClassPathResource resource = new ClassPathResource("gollm/test_input.json");
		String content = new String(Files.readAllBytes(resource.getFile().toPath()));

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript("gollm:model_card");
		req.setInput(content.getBytes());

		List<TaskResponse> responses = taskService.runTaskBlocking(req, 300);

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());

		for (TaskResponse resp : responses) {
			Assertions.assertEquals(taskId, resp.getId());
		}

		log.info(new String(responses.get(responses.size() - 1).getOutput()));
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSendGoLLMEmbeddingRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript("gollm:embedding");
		req.setInput(
				("{\"text\":\"What kind of dinosaur is the coolest?\",\"embedding_model\":\"text-embedding-ada-002\"}")
						.getBytes());

		List<TaskResponse> responses = taskService.runTaskBlocking(req);

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());

		for (TaskResponse resp : responses) {
			Assertions.assertEquals(taskId, resp.getId());
		}
	}

}
