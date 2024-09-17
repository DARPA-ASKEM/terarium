package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.FrameworkService;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

public class FrameworkControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FrameworkService frameworkService;

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
	public void testItCanCreateFramework() throws Exception {
		final ModelFramework framework = new ModelFramework()
			.setName("test-framework")
			.setVersion("0.1.2")
			.setSemantics("test-semantics");

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/models/frameworks")
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(framework))
			)
			.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateFramework() throws Exception {
		final ModelFramework framework = frameworkService.createFramework(
			new ModelFramework().setName("test-framework").setVersion("0.1.2").setSemantics("test-semantics")
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.put("/models/frameworks/" + framework.getId())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(framework))
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetFramework() throws Exception {
		final ModelFramework framework = frameworkService.createFramework(
			new ModelFramework().setName("test-framework").setVersion("0.1.2").setSemantics("test-semantics")
		);

		mockMvc
			.perform(MockMvcRequestBuilders.get("/models/frameworks/" + framework.getId()).with(csrf()))
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteFramework() throws Exception {
		final ModelFramework framework = frameworkService.createFramework(
			new ModelFramework().setName("test-framework").setVersion("0.1.2").setSemantics("test-semantics")
		);

		mockMvc
			.perform(MockMvcRequestBuilders.delete("/models/frameworks/" + framework.getId()).with(csrf()))
			.andExpect(status().isOk());

		Assertions.assertTrue(frameworkService.getFramework(framework.getId()).isEmpty());
	}
}
