package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.ConceptFacetSearchResponse;

public interface OntologyConceptRepositoryCustom {
    public ConceptFacetSearchResponse facetQuery(List<TaggableType> types, List<String> curies);
}
