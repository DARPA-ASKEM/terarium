package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.controller.TrustedController;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.ConceptFacetSearchResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.ConceptFacetSearchResponse.Concept;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ConceptService;

@RequestMapping("/concepts")
@RestController
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ConceptController implements TrustedController {

	final ConceptService conceptService;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Search concepts by curie string")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Concepts found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Concept.class)))),
			@ApiResponse(responseCode = "204", description = "There are no concepts found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving concepts from the data store", content = @Content)
	})
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
	@Operation(summary = "Create a new ontological concept")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Concept created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = OntologyConcept.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the concept", content = @Content)
	})
	public ResponseEntity<OntologyConcept> createConcept(
			@RequestBody final OntologyConcept concept) {

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(conceptService.createConcept(concept));
		} catch (Exception e) {
			final String error = "Unable to create concept";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/definitions")
	@Operation(summary = "Search concept definitions by term")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Concepts found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Concept.class)))),
			@ApiResponse(responseCode = "204", description = "There are no concepts found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving concepts from the data store", content = @Content)
	})
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
	@Operation(summary = "Gets concept by curie string")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Concept found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Concept.class))),
			@ApiResponse(responseCode = "204", description = "There was no concept found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the concept from the data store", content = @Content)
	})
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
	@Operation(summary = "Gets concept by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Concept found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Concept.class))),
			@ApiResponse(responseCode = "204", description = "There was no concept found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the concept from the data store", content = @Content)
	})
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
	@Operation(summary = "Deletes a concept")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted concept", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Concept could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
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
	@Operation(summary = "Update a concept")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Concept updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = OntologyConcept.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the concept", content = @Content)
	})
	public ResponseEntity<OntologyConcept> updateConcept(
			@PathVariable("id") final UUID id,
			@RequestBody OntologyConcept concept) {
		try {
			final Optional<OntologyConcept> updated = conceptService.updateConcept(concept.setId(id));
			if (updated.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(updated.get());
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
	@Operation(summary = "Faceted search for concepts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Concepts found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Concept.class)))),
			@ApiResponse(responseCode = "204", description = "There are no concepts found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving concepts from the data store", content = @Content)
	})
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
