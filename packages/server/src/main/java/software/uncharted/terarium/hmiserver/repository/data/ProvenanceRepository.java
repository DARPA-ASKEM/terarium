package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProvenanceRepository extends PSCrudRepository<Provenance, UUID> {

    List<Provenance> findAllByDeletedOnIsNull();

    List<Provenance> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

    Optional<Provenance> getByIdAndDeletedOnIsNull(final UUID id);
}
