package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

public interface InterventionRepository extends PSCrudSoftDeleteRepository<Intervention, UUID> {}
