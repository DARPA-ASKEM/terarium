package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationEngine;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationUpdate;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

public class SimulationServiceTests extends TerariumApplicationTests {

	@Autowired
	SimulationService simulationService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSearchService projectSearchService;

	Project project;

	static ObjectMapper objectMapper = new ObjectMapper();

	static SimulationRequest createSimRequest() {
		final SimulationRequest request = new SimulationRequest();
		request.setModelConfigId(UUID.randomUUID());
		request.setTimespan(new TimeSpan());
		return request;
	}

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

	static Simulation createSimulation(final String key) {
		final Simulation simulation = new Simulation();
		simulation.setName("test-simulation-name-" + key);
		simulation.setDescription("test-simulation-description-" + key);
		simulation.setType(SimulationType.SIMULATION);
		simulation.setExecutionPayload(objectMapper.convertValue(createSimRequest(), JsonNode.class));
		simulation.setResultFiles(Arrays.asList("never", "gonna", "give", "you", "up"));
		simulation.setStatus(ProgressState.RUNNING);
		simulation.setEngine(SimulationEngine.SCIML);
		simulation.setPublicAsset(true);

		return simulation;
	}

	static SimulationUpdate createSimulationUpdate(final JsonNode data) {
		final SimulationUpdate update = new SimulationUpdate();
		update.setData(data);
		return update;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateSimulation() {
		final Simulation before = (Simulation) createSimulation("0").setId(UUID.randomUUID());
		try {
			final Simulation after = simulationService.createAsset(before, project.getId(), ASSUME_WRITE_PERMISSION);

			Assertions.assertEquals(before.getId(), after.getId());
			Assertions.assertNotNull(after.getId());
			Assertions.assertNotNull(after.getCreatedOn());
		} catch (final Exception e) {
			Assertions.fail(e);
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCantCreateDuplicates() {
		final Simulation simulation = (Simulation) createSimulation("0").setId(UUID.randomUUID());
		try {
			simulationService.createAsset(simulation, project.getId(), ASSUME_WRITE_PERMISSION);
			simulationService.createAsset(simulation, project.getId(), ASSUME_WRITE_PERMISSION);
			Assertions.fail("Should have thrown an exception");
		} catch (final Exception e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetSimulations() throws IOException {
		simulationService.createAsset(createSimulation("0"), project.getId(), ASSUME_WRITE_PERMISSION);
		simulationService.createAsset(createSimulation("1"), project.getId(), ASSUME_WRITE_PERMISSION);
		simulationService.createAsset(createSimulation("2"), project.getId(), ASSUME_WRITE_PERMISSION);

		final List<Simulation> sims = simulationService.getPublicNotTemporaryAssets(0, 10);

		Assertions.assertEquals(3, sims.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetSimulationById() throws IOException {
		final Simulation simulation = simulationService.createAsset(
			createSimulation("0"),
			project.getId(),
			ASSUME_WRITE_PERMISSION
		);
		final Simulation fetchedSimulation = simulationService.getAsset(simulation.getId(), ASSUME_WRITE_PERMISSION).get();

		Assertions.assertEquals(simulation, fetchedSimulation);
		Assertions.assertEquals(simulation.getId(), fetchedSimulation.getId());
		Assertions.assertEquals(simulation.getCreatedOn(), fetchedSimulation.getCreatedOn());
		Assertions.assertEquals(simulation.getUpdatedOn(), fetchedSimulation.getUpdatedOn());
		Assertions.assertEquals(simulation.getDeletedOn(), fetchedSimulation.getDeletedOn());
		Assertions.assertEquals(simulation.getEngine(), fetchedSimulation.getEngine());
		Assertions.assertEquals(simulation.getType(), fetchedSimulation.getType());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateSimulation() throws Exception {
		final Simulation simulation = simulationService.createAsset(
			createSimulation("A"),
			project.getId(),
			ASSUME_WRITE_PERMISSION
		);
		simulation.setName("new name");

		final Simulation updatedSimulation = simulationService
			.updateAsset(simulation, project.getId(), ASSUME_WRITE_PERMISSION)
			.orElseThrow();

		Assertions.assertEquals(simulation, updatedSimulation);
		Assertions.assertNotNull(updatedSimulation.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteSimulation() throws Exception {
		final Simulation simulation = simulationService.createAsset(
			createSimulation("B"),
			project.getId(),
			ASSUME_WRITE_PERMISSION
		);

		simulationService.deleteAsset(simulation.getId(), project.getId(), ASSUME_WRITE_PERMISSION);

		final Optional<Simulation> deleted = simulationService.getAsset(simulation.getId(), ASSUME_WRITE_PERMISSION);

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneSimulation() throws Exception {
		Simulation simulation = createSimulation("A");

		simulation = simulationService.createAsset(simulation, project.getId(), ASSUME_WRITE_PERMISSION);

		final Simulation cloned = simulation.clone();

		Assertions.assertNotEquals(simulation.getId(), cloned.getId());
		Assertions.assertEquals(simulation.getName(), cloned.getName());
		Assertions.assertEquals(simulation.getResultFiles().size(), cloned.getResultFiles().size());
		Assertions.assertEquals(simulation.getExecutionPayload(), cloned.getExecutionPayload());
		Assertions.assertEquals(simulation.getType(), cloned.getType());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateSimulationUpdates() {
		final Simulation before = (Simulation) createSimulation("0").setId(UUID.randomUUID());

		try {
			Simulation after = simulationService.createAsset(before, project.getId(), ASSUME_WRITE_PERMISSION);

			final String jsonString = "{\"key\":\"value\"}";
			final JsonNode data = objectMapper.readTree(jsonString);

			SimulationUpdate update0 = createSimulationUpdate(data);
			update0 = simulationService.appendUpdateToSimulation(after.getId(), update0, ASSUME_WRITE_PERMISSION);
			Assertions.assertNotNull(update0.getCreatedOn());
			Assertions.assertNotNull(update0.getUpdatedOn());

			SimulationUpdate update1 = createSimulationUpdate(data);
			update1 = simulationService.appendUpdateToSimulation(after.getId(), update1, ASSUME_WRITE_PERMISSION);
			Assertions.assertNotNull(update1.getCreatedOn());
			Assertions.assertNotNull(update1.getUpdatedOn());

			after = simulationService.getAsset(after.getId(), ASSUME_WRITE_PERMISSION).orElseThrow();

			Assertions.assertEquals(2, after.getUpdates().size());

			final Simulation again = simulationService.getAsset(after.getId(), ASSUME_WRITE_PERMISSION).get();
			Assertions.assertEquals(2, again.getUpdates().size());
		} catch (final Exception e) {
			Assertions.fail(e);
		}
	}
}
