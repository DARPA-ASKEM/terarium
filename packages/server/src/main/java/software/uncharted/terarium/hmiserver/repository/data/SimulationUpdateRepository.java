package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationUpdate;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

public interface SimulationUpdateRepository extends PSCrudRepository<SimulationUpdate, UUID> {}
