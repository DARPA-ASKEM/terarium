package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

public interface CodeRepository extends PSCrudSoftDeleteRepository<Code, UUID> {}
