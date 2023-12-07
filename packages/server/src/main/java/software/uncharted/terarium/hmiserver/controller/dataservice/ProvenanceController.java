package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.NotImplementedException;
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
	public ResponseEntity<ProvenanceSearchResult> searchChildNodes(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.childNodes(body));
	}

	@PostMapping("/search/parent_nodes")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchParentNodes(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.parentNodes(body));
	}

	@PostMapping("/search/connected_nodes")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchConnectedNodes(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.connectedNodes(body));
	}

	@PostMapping("/search/parent_model_revisions")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchParentModelRevisions(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.parentModelRevisions(body));
	}

	@PostMapping("/search/parent_models")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchParentModels(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.parentModels(body));
	}

	@PostMapping("/search/concept")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchConcept(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.concept(body));
	}

	@PostMapping("/search/artifacts_created_by_user")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchArtifactsCreatedByUser(
			@RequestBody final ProvenanceQueryParam body) {

		return ResponseEntity.ok(provenanceSearchService.artifactsCreatedByUser(body));
	}

	@PostMapping("/search/models_from_code")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchModelsFromCode(
			@RequestBody final ProvenanceQueryParam body) {
		throw new NotImplementedException();
	}

	@PostMapping("/search/models_from_equation")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchModelsFromEquation(
			@RequestBody final ProvenanceQueryParam body) {
		throw new NotImplementedException();
	}

	@PostMapping("/search/extracted_models")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchExtractedModels(
			@RequestBody final ProvenanceQueryParam body) {
		throw new NotImplementedException();
	}

	@PostMapping("/search/model_document")
	@Secured(Roles.USER)
	public ResponseEntity<ProvenanceSearchResult> searchModelDocument(
			@RequestBody final ProvenanceQueryParam body) {
		throw new NotImplementedException();
	}

	@DeleteMapping("/hanging_nodes")
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
