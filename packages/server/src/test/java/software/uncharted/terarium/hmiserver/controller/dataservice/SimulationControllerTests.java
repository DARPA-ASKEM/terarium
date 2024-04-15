package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class SimulationControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SimulationService simulationAssetService;

	@Autowired
	private ElasticsearchService elasticService;

	@Autowired
	private ElasticsearchConfiguration elasticConfig;

	@BeforeEach
	public void setup() throws IOException {
		elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getSimulationIndex());
	}

	@AfterEach
	public void teardown() throws IOException {
		elasticService.deleteIndex(elasticConfig.getSimulationIndex());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateSimulation() throws Exception {

		final Simulation simulationAsset = new Simulation()
				.setName("test-simulation-name")
				.setDescription("my description");

		mockMvc.perform(MockMvcRequestBuilders.post("/simulations")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(simulationAsset)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetSimulation() throws Exception {

		final Simulation simulationAsset = simulationAssetService.createSimulation(new Simulation()
				.setName("test-simulation-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/simulations/" + simulationAsset.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteSimulation() throws Exception {

		final Simulation simulationAsset = simulationAssetService.createSimulation(new Simulation()
				.setName("test-simulation-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.delete("/simulations/" + simulationAsset.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(simulationAssetService.getSimulation(simulationAsset.getId()).isEmpty());
	}

}
