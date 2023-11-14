package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ConceptProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;


@RequestMapping("/concepts")
@RestController
@Slf4j
public class ConceptController {

	@Autowired
	ConceptProxy proxy;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> searchConcept(
		@RequestParam("curie") final String curie
	) {
		try {
			return ResponseEntity.ok(proxy.searchConcept(curie).getBody());
		} catch (RuntimeException e) {
			log.error("Unable to get search concept", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createConcept(
		@RequestBody final Concept concept
	) {
		try {
			return ResponseEntity.ok(proxy.createConcept(concept).getBody());
		} catch (RuntimeException e) {
			log.error("Unable to create a concept", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/definitions")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> searchConceptDefinitions(
		@RequestParam("term") String term,
		@RequestParam(name = "limit", defaultValue = "100") Integer limit,
		@RequestParam(name = "offset", defaultValue = "100") Integer offset
	) {
		try {
			return ResponseEntity.ok(proxy.searchConceptDefinitions(term, limit, offset).getBody());
		} catch (RuntimeException e) {
			log.error("An error searching concept definitions", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/definitions/{curie}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> getConceptDefinitions(
		@PathVariable("curie") final String curie
	) {
		try {
			return ResponseEntity.ok(proxy.getConceptDefinitions(curie).getBody());
		} catch (RuntimeException e) {
			log.error("An error getting concept definitions", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> getConcept(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(proxy.getConcept(id).getBody());
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> deleteConcept(
		@PathVariable("id") final String id
	) {
		try {
			return ResponseEntity.ok(proxy.deleteConcept(id).getBody());
		} catch (RuntimeException e) {
			log.error("Unable to delete concept", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> updateConcept(
		@PathVariable("id") final String id,
		@RequestBody final Concept concept
	) {
		try {
			return ResponseEntity.ok(proxy.updateConcept(id, concept).getBody());
		} catch (RuntimeException e) {
			log.error("Unable to update concept", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/facets")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> searchConceptsUsingFacets(
		@RequestParam(value = "types", required = false) final List<String> types,
		@RequestParam(value = "curies", required = false) final List<String> curies
	) {
		try {
			return ResponseEntity.ok(proxy.searchConceptsUsingFacets(types, curies).getBody());
		} catch (RuntimeException e) {
			log.error("Unable to search concept using facets", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
