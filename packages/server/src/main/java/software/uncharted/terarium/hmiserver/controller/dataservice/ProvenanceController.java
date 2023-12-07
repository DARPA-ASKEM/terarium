package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.lang.reflect.Method;
import java.util.Optional;

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
	public ResponseEntity<Provenance> getProvenance(@PathVariable("id") String id) {
		Optional<Provenance> provenance = provenanceService.getProvenance(id);

		if (provenance.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Provenance %s not found", id));
		}

		return ResponseEntity.ok(provenance.get());
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<ResponseId> createProvenance(
			@RequestBody final Provenance provenance) {
		Provenance createdProvenance = provenanceService.createProvenance(provenance);
		return ResponseEntity.ok(new ResponseId().setId(createdProvenance.getId()));
	}

	@PostMapping("/search")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> searchProvenance(
			@RequestParam final String searchType,
			@RequestBody final ProvenanceQueryParam body) {

		Method method;
		try {
			method = ProvenanceSearchService.class.getMethod(searchType);
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

	@PostMapping("/connected-nodes")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchConnectedNodes(
			@RequestBody final ProvenanceQueryParam body,
			@RequestParam(name = "search_type", defaultValue = "connected_nodes") String searchType) {

		return ResponseEntity.ok(provenanceSearchService.connectedNodes(body));
	}

	@DeleteMapping("/hanging-nodes")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseSuccess> deleteHangingNodes() {
		provenanceService.deleteHangingNodes();

		return ResponseEntity.ok(new ResponseSuccess().setSuccess(true));
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseDeleted> deleteProvenance(
			@PathVariable("id") final String id) {

		provenanceService.deleteProvenance(id);
		return ResponseEntity.ok(new ResponseDeleted("Provenance", id));
	}
}
