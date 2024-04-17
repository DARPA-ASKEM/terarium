package software.uncharted.terarium.hmiserver.repository.data;

import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

import java.util.UUID;

public interface SimulationRepository extends PSCrudSoftDeleteRepository<Simulation, UUID>{
}
