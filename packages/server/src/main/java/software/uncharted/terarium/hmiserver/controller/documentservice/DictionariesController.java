package software.uncharted.terarium.hmiserver.controller.documentservice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDDictionariesResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.security.Roles;


@RestController
@Slf4j
public class DictionariesController {

	@Autowired
	DocumentProxy proxy;

	@GetMapping("/dictionaries")
	@Secured(Roles.USER)
	public ResponseEntity<XDDResponse<XDDDictionariesResponseOK>> getAvailableDictionaries(@RequestParam(name = "all", defaultValue = "") final String all) {
		try {
			XDDResponse<XDDDictionariesResponseOK> response = proxy.getAvailableDictionaries(all);

			if (response.getErrorMessage() != null) {
				return ResponseEntity.internalServerError().build();
			}


			if (response.getSuccess() == null || response.getSuccess().getData().isEmpty())
				return ResponseEntity.noContent().build();

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			log.error("Unable to get available dictionaries", e);
			return ResponseEntity.internalServerError().build();
		}
	}

}
