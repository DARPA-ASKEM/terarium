package software.uncharted.terarium.hmiserver.service.data;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.ActiveConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.ConceptFacetSearchResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.proxies.mira.MIRAProxy;
import software.uncharted.terarium.hmiserver.repository.data.ActiveConceptRepository;
import software.uncharted.terarium.hmiserver.repository.data.OntologyConceptRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConceptService {

  final OntologyConceptRepository ontologyConceptRepository;
  final ActiveConceptRepository activeConceptRespository;
  final MIRAProxy miraProxy;

  public List<OntologyConcept> getConcepts() {
    return ontologyConceptRepository.findAllByDeletedOnIsNull();
  }

  public List<OntologyConcept> getConcepts(final List<UUID> ids) {
    return ontologyConceptRepository.findAllByIdInAndDeletedOnIsNull(ids);
  }

  public List<OntologyConcept> searchConcept(final String curie) {
    return ontologyConceptRepository.findAllByCurieAndDeletedOnIsNull(curie);
  }

  public Optional<OntologyConcept> getConcept(final UUID id) {
    return ontologyConceptRepository.getByIdAndDeletedOnIsNull(id);
  }

  public OntologyConcept createConcept(final OntologyConcept concept) {
    markConceptAsActive(concept);
    return ontologyConceptRepository.save(concept);
  }

  public Optional<OntologyConcept> updateConcept(final OntologyConcept concept) {
    if (!ontologyConceptRepository.existsById(concept.getId())) {
      return Optional.empty();
    }
    ActiveConcept active = markConceptAsActive(concept);
    concept.setActiveConcept(active);
    return Optional.of(ontologyConceptRepository.save(concept));
  }

  public void deleteConcept(final UUID id) {
    Optional<OntologyConcept> concept = ontologyConceptRepository.findById(id);
    if (concept.isEmpty()) {
      return;
    }
    concept.get().setDeletedOn(Timestamp.from(Instant.now()));
    updateConcept(concept.get());
  }

  public List<DKG> searchConceptDefinitions(String term, Integer limit, Integer offset)
      throws Exception {
    return miraProxy.search(term, limit, offset).getBody();
  }

  public DKG getConceptDefinition(String curie) throws Exception {
    return miraProxy.getEntity(curie).getBody();
  }

  public ConceptFacetSearchResponse searchConceptsUsingFacets(
      List<TaggableType> types, List<String> curies) {

    return ontologyConceptRepository.facetQuery(types, curies);
  }

  private ActiveConcept markConceptAsActive(final OntologyConcept concept) {
    Optional<ActiveConcept> activeOptional =
        activeConceptRespository.getByCurie(concept.getCurie());
    ActiveConcept active = null;
    if (activeOptional.isEmpty()) {

      active = new ActiveConcept();
      active.setCurie(concept.getCurie());

      try {
        DKG dkg = getConceptDefinition(concept.getCurie());
        active.setName(dkg.getName());
      } catch (Exception e) {
        log.error("Unable to fetch from DKG", e);
      }

      activeConceptRespository.save(active);
    } else {
      active = activeOptional.get();
    }
    return active;
  }
}
