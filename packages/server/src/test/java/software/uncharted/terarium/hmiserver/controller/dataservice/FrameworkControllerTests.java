package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.service.data.FrameworkService;

public class FrameworkControllerTests extends TerariumApplicationTests {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FrameworkService frameworkService;

	final ModelFramework framework = new ModelFramework()
			.setName("test-framework")
			.setVersion("0.1.2")
			.setSemantics("test-semantics");

	@After
	public void tearDown() {
		frameworkService.deleteFramework(framework.getName());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateFramework() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/models/frameworks")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(framework)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateFramework() throws Exception {

		frameworkService.createFramework(framework);

		mockMvc.perform(MockMvcRequestBuilders.put("/models/frameworks/" + framework.getName())
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(framework)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetFramework() throws Exception {

		frameworkService.createFramework(framework);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/frameworks/" + framework.getName())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteFramework() throws Exception {

		frameworkService.createFramework(framework);

		mockMvc.perform(MockMvcRequestBuilders.delete("/models/frameworks/" + framework.getName())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(frameworkService.getFramework(framework.getName()));
	}
}
