package software.uncharted.terarium.hmiserver.repository.data;

import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;
import java.util.UUID;

public interface EquationRepository extends PSCrudSoftDeleteRepository<Equation, UUID> {}
