package software.uncharted.terarium.hmiserver.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DKGService;

@RequestMapping("/dkg")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DKGController {

	private final DKGService dkgService;

	@GetMapping("/search")
	@Secured(Roles.USER)
	public ResponseEntity<List<DKG>> searchAssets(
		@RequestParam(required = false, defaultValue = "0") final Integer page,
		@RequestParam(required = false, defaultValue = "10") final Integer pageSize,
		@RequestParam final String term
	) {
		try {
			return ResponseEntity.ok(dkgService.searchEpiDKG(page, pageSize, term, null));
		} catch (Exception e) {
			log.error("Error searching assets", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/currie/{curie}")
	@Secured(Roles.USER)
	public ResponseEntity<List<DKG>> getEntity(@PathVariable("curie") final String curie) {
		try {
			return ResponseEntity.ok(dkgService.getEpiEntity(curie));
		} catch (Exception e) {
			log.error("Error getting entity", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
