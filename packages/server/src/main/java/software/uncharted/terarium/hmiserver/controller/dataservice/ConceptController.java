package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.data.concept.ConceptFacetSearchResponse;
import software.uncharted.terarium.hmiserver.models.data.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ConceptService;

@RequestMapping("/concepts")
@RestController
@Slf4j
public class ConceptController {

	@Autowired
	ConceptService conceptService;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<OntologyConcept>> searchConcept(
			@RequestParam("curie") final String curie) {
		try {
			return ResponseEntity.ok(conceptService.searchConcept(curie));
		} catch (RuntimeException e) {
			log.error("Unable to get search concept", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<OntologyConcept> createConcept(
			@RequestBody final OntologyConcept concept) {

		try {
			return ResponseEntity.ok(conceptService.createConcept(concept));
		} catch (Exception e) {
			final String error = "Unable to create concept";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/definitions")
	@Secured(Roles.USER)
	public ResponseEntity<DKG> searchConceptDefinitions(
			@RequestParam("term") String term,
			@RequestParam(name = "limit", defaultValue = "100") Integer limit,
			@RequestParam(name = "offset", defaultValue = "100") Integer offset) {
		try {
			return ResponseEntity.ok(conceptService.searchConceptDefinitions(term, limit, offset));
		} catch (Exception e) {
			final String error = "Unable to search concept definitions";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/definitions/{curie}")
	@Secured(Roles.USER)
	public ResponseEntity<DKG> getConceptDefinition(
			@PathVariable("curie") final String curie) {
		try {
			return ResponseEntity.ok(conceptService.getConceptDefinition(curie));
		} catch (Exception e) {
			final String error = "Unable to get concept definition";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<OntologyConcept> getConcept(
			@PathVariable("id") final UUID id) {

		try {
			Optional<OntologyConcept> concept = conceptService.getConcept(id);
			if (concept.isPresent()) {
				return ResponseEntity.ok(concept.get());
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			final String error = "Unable to get concept";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseDeleted> deleteConcept(
			@PathVariable("id") final UUID id) {

		try {
			conceptService.deleteConcept(id);
			return ResponseEntity.ok(new ResponseDeleted("Concept", id));
		} catch (Exception e) {
			final String error = "Unable to delete concept";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<OntologyConcept> updateConcept(
			@PathVariable("id") final UUID id,
			@RequestBody OntologyConcept concept) {
		try {
			return ResponseEntity.ok(conceptService.updateConcept(concept.setId(id)));
		} catch (RuntimeException e) {
			final String error = "Unable to update concept";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/facets")
	@Secured(Roles.USER)
	public ResponseEntity<ConceptFacetSearchResponse> searchConceptsUsingFacets(
			@RequestParam(value = "types", required = false) final List<TaggableType> types,
			@RequestParam(value = "curies", required = false) final List<String> curies) {
		try {
			return ResponseEntity.ok(conceptService.searchConceptsUsingFacets(types, curies));
		} catch (RuntimeException e) {
			final String error = "Unable to search concepts using facets";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}
}
