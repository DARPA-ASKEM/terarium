package software.uncharted.terarium.hmiserver.controller.funman;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.funman.FunmanPostQueriesRequest;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RestController
@RequestMapping("/funman/queries")
@RequiredArgsConstructor
@Slf4j
public class FunmanController {

	private final FunmanProxy funmanProxy;

	@GetMapping("/{queryId}/halt")
	@Secured(Roles.USER)
	@ApiResponses(
			value = {
				@ApiResponse(responseCode = "200", description = "Query halted"),
			})
	public ResponseEntity<JsonNode> halt(@PathVariable final String queryId) {
		try {
			final ResponseEntity<JsonNode> response = funmanProxy.halt(queryId);
			return ResponseEntity.ok(response.getBody());
		} catch (final FeignException e) {
			final String error = "Error halting";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}
	}

	@GetMapping("/{queryId}")
	@Secured(Roles.USER)
	@ApiResponses(
			value = {
				@ApiResponse(responseCode = "200", description = "Query found"),
			})
	public ResponseEntity<JsonNode> getQueries(@PathVariable final String queryId) {
		try {
			final ResponseEntity<JsonNode> response = funmanProxy.getQueries(queryId);
			return ResponseEntity.ok(response.getBody());
		} catch (final FeignException e) {
			final String error = "Error getting query";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@ApiResponses(
			value = {
				@ApiResponse(responseCode = "200", description = "Query posted"),
			})
	public ResponseEntity<JsonNode> postQueries(@RequestBody final FunmanPostQueriesRequest requestBody) {
		try {
			final ResponseEntity<JsonNode> response = funmanProxy.postQueries(requestBody);
			return ResponseEntity.ok(response.getBody());
		} catch (final FeignException e) {
			final String error = "Error posting query";
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}
	}
}
