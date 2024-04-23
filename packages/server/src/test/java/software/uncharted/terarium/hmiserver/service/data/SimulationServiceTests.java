package software.uncharted.terarium.hmiserver.service.data;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;

import java.util.UUID;

public class SimulationServiceTests extends TerariumApplicationTests {

	@Autowired
	SimulationService simulationService;


	static Simulation createSimulation(String key) {
		Simulation simulation = new Simulation();
		simulation.setName("test-simulation-name-" + key);
		simulation.setDescription("test-simulation-description-" + key);
		simulation.setType(SimulationType.SIMULATION);

		return simulation;
	}


	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateSimulation() {
		final Simulation before = (Simulation) createSimulation("0").setId(UUID.randomUUID());
		try {
			final Simulation after = simulationService.createAsset(before);

			Assertions.assertEquals(before.getId(), after.getId());
			Assertions.assertNotNull(after.getId());
			Assertions.assertNotNull(after.getCreatedOn());

		} catch(Exception e){
			Assertions.fail(e);
		}


	}

}
