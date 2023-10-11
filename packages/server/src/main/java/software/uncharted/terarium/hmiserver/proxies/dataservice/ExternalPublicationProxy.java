package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.ExternalPublication;

import java.util.List;


@FeignClient(name = "externalPublications", url = "${terarium.dataservice.url}", path = "/external/publications")

public interface ExternalPublicationProxy  {

	@GetMapping
	ResponseEntity<List<ExternalPublication>> getPublications();

	@PostMapping()
	ResponseEntity<JsonNode> createPublication(
		@RequestBody final ExternalPublication publication
	);

	@GetMapping("/{id}")
	ResponseEntity<ExternalPublication> getPublication(
		@PathVariable("id") final Integer id
	);

	@PutMapping("/{id}")
	ResponseEntity<JsonNode> putPublication(
		@PathVariable("id") final Integer id,
		@RequestBody final ExternalPublication publication
	);

	@DeleteMapping("/{id}")
	ResponseEntity<JsonNode> deletePublication(
		@PathVariable("id") final Integer id
	);


}
