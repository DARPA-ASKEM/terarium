package software.uncharted.terarium.hmiserver.controller.task;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;

public class TaskControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateTaskRequest() throws Exception {

		ClassPathResource resource = new ClassPathResource("gollm/test_input.json");
		String input = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

		final TaskRequest req = new TaskRequest()
				.setScript("gollm:model_card")
				.setInput(input.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.post("/task").with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk());

		Thread.sleep(1000 * 10);

	}

}
