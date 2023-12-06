package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.After;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	final ModelHeader header = new ModelHeader()
			.setName("test-name")
			.setModelSchema("test-schema")
			.setModelVersion("0.1.2")
			.setDescription("test-description");

	final Model model = new Model()
			.setId("test-model-id")
			.setHeader(header);

	@After
	public void tearDown() throws IOException {
		modelService.deleteModel(model.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateModel() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/models")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(model)))
				.andExpect(status().isOk());
	}

	// TODO(kbirk): issues with provenance causing this to fail
	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModel() throws Exception {

		modelService.createModel(model);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/" + model.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateModel() throws Exception {
		modelService.createModel(model);

		mockMvc.perform(MockMvcRequestBuilders.put("/models/" + model.getId())
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(model)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteModel() throws Exception {
		modelService.createModel(model);

		mockMvc.perform(MockMvcRequestBuilders.delete("/models/" + model.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelDescription() throws Exception {

		modelService.createModel(model);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/" + model.getId() + "/descriptions")
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelDescriptions() throws Exception {

		modelService.createModel(model);

		mockMvc.perform(MockMvcRequestBuilders.get("/models/descriptions")
				.with(csrf()))
				.andExpect(status().isOk());
	}
}
