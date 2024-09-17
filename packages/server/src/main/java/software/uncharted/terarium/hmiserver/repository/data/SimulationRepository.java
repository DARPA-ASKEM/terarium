package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

public interface SimulationRepository extends PSCrudSoftDeleteRepository<Simulation, UUID> {}
