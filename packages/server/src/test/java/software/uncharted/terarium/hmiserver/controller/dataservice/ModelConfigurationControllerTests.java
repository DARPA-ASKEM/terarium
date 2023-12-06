package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;

public class ModelConfigurationControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ModelConfigurationService modelConfigurationService;

	final ModelConfiguration modelConfiguration = new ModelConfiguration()
			.setId("test-config-id")
			.setName("test-framework")
			.setModelId("test-model-id")
			.setDescription("test-desc")
			.setConfiguration(Map.of("key", "value"));

	@After
	public void tearDown() throws IOException {
		modelConfigurationService.deleteModelConfiguration(modelConfiguration.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelConfiguration() throws Exception {
		modelConfigurationService.createModelConfiguration(modelConfiguration);

		mockMvc.perform(MockMvcRequestBuilders.get("/model_configurations/" + modelConfiguration.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateModelConfiguration() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/model_configurations")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(modelConfiguration)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateModelConfiguration() throws Exception {

		modelConfigurationService.createModelConfiguration(modelConfiguration);

		mockMvc.perform(MockMvcRequestBuilders.put("/model_configurations/" + modelConfiguration.getId())
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(modelConfiguration)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteModelConfiguration() throws Exception {

		modelConfigurationService.createModelConfiguration(modelConfiguration);

		mockMvc.perform(MockMvcRequestBuilders.delete("/model_configurations/" + modelConfiguration.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

}
