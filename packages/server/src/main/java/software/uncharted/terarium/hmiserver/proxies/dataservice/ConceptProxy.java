package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;

import java.util.List;


@FeignClient(name = "concepts", url = "${terarium.dataservice.url}", path = "/concepts")
public interface ConceptProxy {

	//TODO These endpoints are all using the wrong return type. They should be using ResponseEntity<Concept> instead of just JsonNode

	@GetMapping
	ResponseEntity<JsonNode> searchConcept(
		@RequestParam("curie") String curie
	);

	@PostMapping
	ResponseEntity<JsonNode> createConcept(
		@RequestBody Concept concept
	);

	@GetMapping("/definitions")
	ResponseEntity<JsonNode> searchConceptDefinitions(
		@RequestParam("term") String term,
		@RequestParam(name = "limit", defaultValue = "100") Integer limit,
		@RequestParam(name = "offset", defaultValue = "100") Integer offset
	);

	@GetMapping("/definitions/{curie}")
	ResponseEntity<JsonNode> getConceptDefinitions(
		@PathVariable("curie") String curie
	);

	@GetMapping("/{id}")
	ResponseEntity<JsonNode> getConcept(
		@PathVariable("id") String id
	);

	@DeleteMapping("/{id}")
	ResponseEntity<JsonNode> deleteConcept(
		@PathVariable("id") String id
	);

	@PutMapping("/{id}")
	ResponseEntity<JsonNode> updateConcept(
		@PathVariable("id") String id,
		@RequestBody Concept concept
	);

	@GetMapping("/facets")
	ResponseEntity<JsonNode> searchConceptsUsingFacets(
		@RequestParam("types") List<String> types,
		@RequestParam("curies") List<String> curies
	);
}
