package software.uncharted.terarium.hmiserver.service;

import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.controller.mira.MiraController;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.service.TaskService.TaskType;

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

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.GOLLM);

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

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.GOLLM);

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

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.GOLLM, 300);

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());

		for (TaskResponse resp : responses) {
			Assertions.assertEquals(taskId, resp.getId());
		}

		log.info(new String(responses.get(responses.size() - 1).getOutput()));
	}

	static class AdditionalProps {
		public String str;
		public Integer num;
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

		AdditionalProps add = new AdditionalProps();
		add.str = "this is my str";
		add.num = 123;
		req.setAdditionalProperties(add);

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.GOLLM);

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());

		for (TaskResponse resp : responses) {
			Assertions.assertEquals(taskId, resp.getId());

			AdditionalProps respAdd = resp.getAdditionalProperties(AdditionalProps.class);
			Assertions.assertEquals(add.str, respAdd.str);
			Assertions.assertEquals(add.num, respAdd.num);
		}
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSendMiraMDLToStockflowRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		ClassPathResource resource = new ClassPathResource("mira/IndiaNonSubscriptedPulsed.mdl");
		String content = new String(Files.readAllBytes(resource.getFile().toPath()));

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript(MiraController.MDL_TO_STOCKFLOW);
		req.setInput(content.getBytes());

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.MIRA);

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
	public void testItCanSendMiraStellaToStockflowRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		ClassPathResource resource = new ClassPathResource("mira/SIR.xmile");
		String content = new String(Files.readAllBytes(resource.getFile().toPath()));

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript(MiraController.STELLA_TO_STOCKFLOW);
		req.setInput(content.getBytes());

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.MIRA);

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
	public void testItCanSendMiraSBMLToPetrinetRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		ClassPathResource resource = new ClassPathResource("mira/BIOMD0000000001.xml");
		String content = new String(Files.readAllBytes(resource.getFile().toPath()));

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript(MiraController.SBML_TO_PETRINET);
		req.setInput(content.getBytes());

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.MIRA);

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
	public void testItCanSendGoLLMConfigFromDatasetRequest() throws Exception {

		UUID taskId = UUID.randomUUID();

		ClassPathResource datasetResource1 = new ClassPathResource("gollm/Epi Sc 4 Interaction matrix.csv");
		String dataset1 = new String(Files.readAllBytes(datasetResource1.getFile().toPath()));
		ClassPathResource datasetResource2 = new ClassPathResource("gollm/other-dataset.csv");
		String dataset2 = new String(Files.readAllBytes(datasetResource2.getFile().toPath()));

		ClassPathResource amrResource = new ClassPathResource("gollm/scenario4_4spec_regnet_empty.json");
		String amr = new String(Files.readAllBytes(amrResource.getFile().toPath()));
		JsonNode amrJson = new ObjectMapper().readTree(amr);

		String content = "{\"datasets\": ["
				+ "\"" + dataset1.replaceAll("(?<!\\\\)\\n", Matcher.quoteReplacement("\\\\n")) + "\","
				+ "\"" + dataset2.replaceAll("(?<!\\\\)\\n", Matcher.quoteReplacement("\\\\n"))
				+ "\"], \"amr\":"
				+ amrJson.toString() + "}";

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript("gollm:dataset_configure");
		req.setInput(content.getBytes());

		List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.GOLLM);

		Assertions.assertEquals(3, responses.size());
		Assertions.assertEquals(TaskStatus.QUEUED, responses.get(0).getStatus());
		Assertions.assertEquals(TaskStatus.RUNNING, responses.get(1).getStatus());
		Assertions.assertEquals(TaskStatus.SUCCESS, responses.get(2).getStatus());

		for (TaskResponse resp : responses) {
			Assertions.assertEquals(taskId, resp.getId());
		}

		log.info(new String(responses.get(responses.size() - 1).getOutput()));
	}

}
