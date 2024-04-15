package software.uncharted.terarium.hmiserver.repository.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.dataservice.concept.ActiveConcept;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ActiveConceptRepository extends PSCrudRepository<ActiveConcept, UUID> {

	Optional<ActiveConcept> getByCurie(String curie);
}
