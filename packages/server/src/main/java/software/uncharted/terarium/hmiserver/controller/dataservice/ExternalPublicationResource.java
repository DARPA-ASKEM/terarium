package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseResource;
import software.uncharted.terarium.hmiserver.models.dataservice.ExternalPublication;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ExternalPublicationProxy;

import java.util.List;


@RequestMapping("/external/publications")
@RestController
@Slf4j
public class ExternalPublicationResource implements SnakeCaseResource {

	@Autowired
	ExternalPublicationProxy proxy;


	@GetMapping
	public ResponseEntity<List<ExternalPublication>> getPublications() {
		try {
			return ResponseEntity.ok(proxy.getAssets(100, 0).getBody());
		} catch (Exception e) {
			log.error("Unable to get publications", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping()
	public ResponseEntity<JsonNode> createPublication(
		@RequestBody final ExternalPublication publication
	) {
		return ResponseEntity.ok(proxy.createAsset(convertObjectToSnakeCaseJsonNode(publication)).getBody());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ExternalPublication> getPublication(
		@PathVariable("id") final String id
	) {
		try {
			return ResponseEntity.ok(proxy.getAsset(id).getBody());
		} catch (Exception e) {
			log.error("Unable to get publication", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<JsonNode> putPublication(
		@PathVariable("id") final String id,
		@RequestBody final ExternalPublication publication
	) {
		try {
			return ResponseEntity.ok(proxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(publication)).getBody());
		} catch (Exception e) {
			log.error("Unable to put publication", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<JsonNode> deletePublication(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(proxy.deleteAsset(id).getBody());
	}


}
