package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseSuccess;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceSearchResult;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

@RequestMapping("/provenance")
@RestController
@Transactional
public class ProvenanceController {

	@Autowired
	ProvenanceService provenanceService;

	@Autowired
	ProvenanceSearchService provenanceSearchService;

	@PostMapping
	@Secured(Roles.USER)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Provenance entry created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Provenance.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the provenance", content = @Content)
	})
	public ResponseEntity<Provenance> createProvenance(
			@RequestBody Provenance provenance) {
		provenance = provenanceService.createProvenance(provenance);
		return ResponseEntity.status(HttpStatus.CREATED).body(provenance);
	}

	@PostMapping("/search/connected-nodes")
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
}
