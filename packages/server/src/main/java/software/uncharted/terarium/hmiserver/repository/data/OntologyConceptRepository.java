package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface OntologyConceptRepository
        extends PSCrudRepository<OntologyConcept, UUID>, OntologyConceptRepositoryCustom {

    List<OntologyConcept> findAllByCurieAndDeletedOnIsNull(String curieString);

    List<OntologyConcept> findAllByDeletedOnIsNull();

    List<OntologyConcept> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

    Optional<OntologyConcept> getByIdAndDeletedOnIsNull(final UUID id);
}
