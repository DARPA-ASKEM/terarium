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
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.service.data.ModelService;

public class ModelControllerTests extends TerariumApplicationTests {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ModelService modelService;

	@BeforeEach
	public void setup() throws IOException {
		modelService.setupIndexAndAliasAndEnsureEmpty();
	}

	@AfterEach
	public void teardown() throws IOException {
		modelService.teardownIndexAndAlias();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateModel() throws Exception {

		final Model model = new Model()
				.setHeader(new ModelHeader()
						.setName("test-name")
						.setModelSchema("test-schema")
						.setModelVersion("0.1.2")
						.setDescription("test-description")
						.setSchemaName("petrinet"));

		mockMvc.perform(MockMvcRequestBuilders.post("/models")
						.param("project-id", PROJECT_ID.toString())
						.with(csrf())
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(model)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModel() throws Exception {

		final Model model = modelService.createAsset(
				new Model()
						.setHeader(new ModelHeader()
								.setName("test-name")
								.setModelSchema("test-schema")
								.setModelVersion("0.1.2")
								.setDescription("test-description")
								.setSchemaName("petrinet")),
				ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/" + model.getId())
						.param("project-id", PROJECT_ID.toString())
						.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateModel() throws Exception {

		final Model model = modelService.createAsset(
				new Model()
						.setHeader(new ModelHeader()
								.setName("test-name")
								.setModelSchema("test-schema")
								.setModelVersion("0.1.2")
								.setDescription("test-description")
								.setSchemaName("petrinet")),
				ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.put("/models/" + model.getId())
						.param("project-id", PROJECT_ID.toString())
						.with(csrf())
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(model)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteModel() throws Exception {

		final Model model = modelService.createAsset(
				new Model()
						.setHeader(new ModelHeader()
								.setName("test-name")
								.setModelSchema("test-schema")
								.setModelVersion("0.1.2")
								.setDescription("test-description")
								.setSchemaName("petrinet")),
				ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.delete("/models/" + model.getId())
						.param("project-id", PROJECT_ID.toString())
						.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(
				modelService.getAsset(model.getId(), ASSUME_WRITE_PERMISSION).isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelDescription() throws Exception {

		final Model model = modelService.createAsset(
				new Model()
						.setHeader(new ModelHeader()
								.setName("test-name")
								.setModelSchema("test-schema")
								.setModelVersion("0.1.2")
								.setDescription("test-description")
								.setSchemaName("petrinet")),
				ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/" + model.getId() + "/descriptions")
						.param("project-id", PROJECT_ID.toString())
						.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelDescriptions() throws Exception {

		modelService.createAsset(
				new Model()
						.setHeader(new ModelHeader()
								.setName("test-name")
								.setModelSchema("test-schema")
								.setModelVersion("0.1.2")
								.setDescription("test-description")
								.setSchemaName("petrinet")),
				ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/descriptions").with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetPublicModelWithoutProject() throws Exception {

		Model model = new Model()
				.setHeader(new ModelHeader()
						.setName("test-name")
						.setModelSchema("test-schema")
						.setModelVersion("0.1.2")
						.setDescription("test-description")
						.setSchemaName("petrinet"));
		model.setPublicAsset(true);

		Model createdModel = modelService.createAsset(model, ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/" + createdModel.getId())
						.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCannotGetPrivateModelWithoutProject() throws Exception {

		Model model = new Model()
				.setHeader(new ModelHeader()
						.setName("test-name")
						.setModelSchema("test-schema")
						.setModelVersion("0.1.2")
						.setDescription("test-description")
						.setSchemaName("petrinet"));

		Model createdModel = modelService.createAsset(model, ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/" + createdModel.getId())
						.with(csrf()))
				.andExpect(status().is5xxServerError());
	}
}
