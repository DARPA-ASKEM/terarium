package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseSuccess;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceSearchResult;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

@RequestMapping("/provenance")
@RestController
public class ProvenanceController {

	@Autowired
	ProvenanceService provenanceService;

	@Autowired
	ProvenanceSearchService provenanceSearchService;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a provenance entry by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Provenance.class))),
			@ApiResponse(responseCode = "204", description = "There was no provenance found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the provenance from the data store", content = @Content)
	})
	public ResponseEntity<Provenance> getProvenance(@PathVariable("id") UUID id) {
		Optional<Provenance> provenance = provenanceService.getProvenance(id);

		if (provenance.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Provenance %s not found", id));
		}

		return ResponseEntity.ok(provenance.get());
	}

	@PostMapping
	@Secured(Roles.USER)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance entry created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the provenance", content = @Content)
	})
	public ResponseEntity<ResponseId> createProvenance(
			@RequestBody final Provenance provenance) {
		Provenance createdProvenance = provenanceService.createProvenance(provenance);
		return ResponseEntity.ok(new ResponseId(createdProvenance.getId()));
	}

	private String snakeToCamel(String s) {
		Pattern p = Pattern.compile("_(.)");
		Matcher m = p.matcher(s.replaceAll("^_|_$", "").replaceAll("_+", "_"));
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	// We should deprecate this endpoint, it isn't ideal because the different
	// search types have different search response types. Below I have implemented
	// the individual search methods explicitly instead.
	@PostMapping("/search")
	@Secured(Roles.USER)
	@Operation(summary = "Search provenance by name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = JsonNode.class))),
			@ApiResponse(responseCode = "204", description = "There are no provenance entries found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving provenance entries from the data store", content = @Content)
	})
	@Deprecated
	public ResponseEntity<JsonNode> searchProvenance(
			@RequestParam final String searchType,
			@RequestBody final ProvenanceQueryParam body) {

		Method method;
		try {
			method = ProvenanceSearchService.class.getMethod(snakeToCamel(searchType));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Invalid search type: " + searchType);
		}

		try {
			JsonNode response = (JsonNode) method.invoke(provenanceSearchService, body);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new RuntimeException("Error executing search method: " + searchType);
		}
	}

	@PostMapping("/search/child_nodes")
	@Secured(Roles.USER)
	@Operation(summary = "Search child nodes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchChildNodes(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.childNodes(body));
	}

	@PostMapping("/search/parent_nodes")
	@Secured(Roles.USER)
	@Operation(summary = "Search parent nodes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchParentNodes(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.parentNodes(body));
	}

	@PostMapping("/search/connected_nodes")
	@Secured(Roles.USER)
	@Operation(summary = "Search connected nodes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchConnectedNodes(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.connectedNodes(body));
	}

	@PostMapping("/search/parent_model_revisions")
	@Secured(Roles.USER)
	@Operation(summary = "Search model revisions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchParentModelRevisions(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.parentModelRevisions(body));
	}

	@PostMapping("/search/parent_models")
	@Secured(Roles.USER)
	@Operation(summary = "Search parent models")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchParentModels(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.parentModels(body));
	}

	@PostMapping("/search/concept")
	@Secured(Roles.USER)
	@Operation(summary = "Search concepts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchConcept(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.concept(body));
	}

	@PostMapping("/search/artifacts_created_by_user")
	@Secured(Roles.USER)
	@Operation(summary = "Search artifacts created by user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProvenanceSearchResult.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<ProvenanceSearchResult> searchArtifactsCreatedByUser(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.artifactsCreatedByUser(body));
	}

	@PostMapping("/search/concept_counts")
	@Secured(Roles.USER)
	@Operation(summary = "Get concept counts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = JsonNode.class))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<Map<String, Integer>> searchConceptCounts(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.conceptCounts(body));
	}

	@PostMapping("/search/models_from_code")
	@Secured(Roles.USER)
	@Operation(summary = "Search models from code")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<Set<String>> searchModelsFromCode(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.modelsFromCode(body));
	}

	@PostMapping("/search/models_from_equation")
	@Secured(Roles.USER)
	@Operation(summary = "Search models from equations")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<Set<String>> searchModelsFromEquation(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.modelsFromEquation(body));
	}

	@PostMapping("/search/models_from_document")
	@Secured(Roles.USER)
	@Operation(summary = "Search models from documents")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<Set<String>> searchModelsFromDocument(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.modelsFromDocument(body));
	}

	@PostMapping("/search/extracted_models")
	@Secured(Roles.USER)
	@Operation(summary = "Search extracted models")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<Set<String>> searchExtractedModels(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.extractedModels(body));
	}

	@PostMapping("/search/model_document")
	@Secured(Roles.USER)
	@Operation(summary = "Search for a models document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Provenance results found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "204", description = "There are no results found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving results from the data store", content = @Content)
	})
	public ResponseEntity<Map<String, Object>> searchModelDocument(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.modelDocument(body));
	}

	@DeleteMapping("/hanging_nodes")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes all hanging nodes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Whether the delete was successful", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseSuccess.class)) }),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	public ResponseEntity<ResponseSuccess> deleteHangingNodes() {
		provenanceService.deleteHangingNodes();

		return ResponseEntity.ok(new ResponseSuccess(true));
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a provenance entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted entry", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Provenance could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	public ResponseEntity<ResponseDeleted> deleteProvenance(
			@PathVariable("id") final UUID id) {

		provenanceService.deleteProvenance(id);
		return ResponseEntity.ok(new ResponseDeleted("Provenance", id));
	}
}
