package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.data.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.data.project.ResourceType;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface OntologyConceptRepository
		extends PSCrudRepository<OntologyConcept, UUID>, OntologyConceptRepositoryCustom {

	List<OntologyConcept> listAllByCurie(String curieString);

	List<OntologyConcept> findAllByTypes(List<ResourceType> types);

}
