package software.uncharted.terarium.hmiserver.controller.documentservice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDSetsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/document")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SetController {

	final DocumentProxy proxy;

	@GetMapping("/sets")
	@Secured(Roles.USER)
	public ResponseEntity<XDDSetsResponse> getAvailableSets() {
		try {
			final XDDSetsResponse response = proxy.getAvailableSets();

			if (
				response.getAvailableSets() == null || response.getAvailableSets().isEmpty()
			) return ResponseEntity.noContent().build();

			return ResponseEntity.ok(response);
		} catch (final FeignException e) {
			log.error("xDD returned an exception for set search:", e);
			throw new ResponseStatusException(
				HttpStatusCode.valueOf(e.status()),
				"There was an issue with the request to xDD"
			);
		} catch (final Exception e) {
			log.error("Unable to find sets, an error occurred", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to find documents, an error occurred"
			);
		}
	}
}
