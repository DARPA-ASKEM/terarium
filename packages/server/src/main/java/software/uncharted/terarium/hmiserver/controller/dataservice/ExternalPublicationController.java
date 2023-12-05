package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.ExternalPublication;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ExternalPublicationProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;


@RequestMapping("/external/publications")
@RestController
@Slf4j
public class ExternalPublicationController{

	@Autowired
	ExternalPublicationProxy proxy;


	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<ExternalPublication>> getPublications() {
		try {
			return ResponseEntity.ok(proxy.getPublications().getBody());
		} catch (Exception e) {
			log.error("Unable to get publications", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping()
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createPublication(
		@RequestBody final ExternalPublication publication
	) {
		return ResponseEntity.ok(proxy.createPublication(publication).getBody());
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ExternalPublication> getPublication(
		@PathVariable("id") final Integer id
	) {
		try {
			return ResponseEntity.ok(proxy.getPublication(id).getBody());
		} catch (Exception e) {
			log.error("Unable to get publication", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get publication");
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> putPublication(
		@PathVariable("id") final Integer id,
		@RequestBody final ExternalPublication publication
	) {
		try {
			return ResponseEntity.ok(proxy.putPublication(id, publication).getBody());
		} catch (Exception e) {
			log.error("Unable to put publication", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to put publication");
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> deletePublication(
		@PathVariable("id") final Integer id
	) {
		return ResponseEntity.ok(proxy.deletePublication(id).getBody());
	}


}
