package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

public class ModelConfigurationControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ModelConfigurationService modelConfigurationService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSearchService projectSearchService;

	Project project;

	@BeforeEach
	public void setup() throws IOException {
		projectSearchService.setupIndexAndAliasAndEnsureEmpty();
		project = projectService.createProject(
			(Project) new Project().setPublicAsset(true).setName("test-project-name").setDescription("my description")
		);
	}

	@AfterEach
	public void teardown() throws IOException {
		projectSearchService.teardownIndexAndAlias();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelConfiguration() throws Exception {
		final ModelConfiguration modelConfiguration = modelConfigurationService.createAsset(
			(ModelConfiguration) new ModelConfiguration()
				.setModelId(UUID.randomUUID())
				.setName("test-framework")
				.setDescription("test-desc"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.get("/model-configurations/" + modelConfiguration.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateModelConfiguration() throws Exception {
		final ModelConfiguration modelConfiguration = (ModelConfiguration) new ModelConfiguration()
			.setModelId(UUID.randomUUID())
			.setDescription("test-desc")
			.setName("test-framework");

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/model-configurations")
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(modelConfiguration))
			)
			.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateModelConfiguration() throws Exception {
		final ModelConfiguration modelConfiguration = modelConfigurationService.createAsset(
			(ModelConfiguration) new ModelConfiguration()
				.setModelId(UUID.randomUUID())
				.setDescription("test-desc")
				.setName("test-framework"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.put("/model-configurations/" + modelConfiguration.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(modelConfiguration))
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteModelConfiguration() throws Exception {
		final ModelConfiguration modelConfiguration = (ModelConfiguration) new ModelConfiguration()
			.setModelId(UUID.randomUUID())
			.setDescription("test-desc")
			.setName("test-framework");

		mockMvc
			.perform(
				MockMvcRequestBuilders.delete("/model-configurations/" + modelConfiguration.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		Assertions.assertTrue(modelConfigurationService.getAsset(modelConfiguration.getId()).isEmpty());
	}
}
