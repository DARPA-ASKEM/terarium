package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;


@FeignClient(name = "provenance", url = "${terarium.dataservice.url}", path = "/provenance")
public interface ProvenanceProxy {
	@GetMapping("/{id}")
	ResponseEntity<Provenance> getProvenance(@PathVariable("id") String id);

	@PostMapping
	ResponseEntity<JsonNode> createProvenance(
		@RequestBody Provenance provenance
	);

	@PostMapping("/search")
	ResponseEntity<JsonNode> search(
		@RequestBody ProvenanceQueryParam body,
		@RequestParam("search_type") String searchType
	);

	@DeleteMapping("/hanging_nodes")
	ResponseEntity<JsonNode> deleteHangingNodes();

	@DeleteMapping("/{id}")
	ResponseEntity<JsonNode> deleteProvenance(
		@PathVariable("id") String id
	);
}
