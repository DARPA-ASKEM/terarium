package software.uncharted.terarium.hmiserver.controller.documentservice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDSetsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/document")
@RestController
@Slf4j
public class SetController {

	@Autowired
	DocumentProxy proxy;

	@GetMapping("/sets")
	@Secured(Roles.USER)
	public ResponseEntity<XDDSetsResponse> getAvailableSets() {

		try {
			XDDSetsResponse response = proxy.getAvailableSets();

			if (response.getAvailableSets() == null || response.getAvailableSets().isEmpty())
				return ResponseEntity.noContent().build();

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			log.error("There was an error finding available sets", e);
			return ResponseEntity.internalServerError().build();
		}
	}

}
