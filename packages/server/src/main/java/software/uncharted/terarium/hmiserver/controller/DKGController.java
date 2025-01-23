package software.uncharted.terarium.hmiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.ContextMatcher;
import software.uncharted.terarium.hmiserver.service.data.DKGService;

@RequestMapping("/dkg")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DKGController {

	private final DKGService dkgService;

	@GetMapping("/search")
	@Secured(Roles.USER)
	@Operation(summary = "Search for DKG entities using text in the EpiDKG index")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Search results",
				content = @Content(
					array = @io.swagger.v3.oas.annotations.media.ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DKG.class)
					)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue with the search", content = @Content)
		}
	)
	public ResponseEntity<Grounding> searchAssets(
		@RequestParam(required = false, defaultValue = "0") final Integer page,
		@RequestParam(required = false, defaultValue = "10") final Integer pageSize,
		@RequestParam final String term
	) {
		// First pass, search for exact matches using the curated contexts
		Grounding grounding = ContextMatcher.searchBest(term);

		// If grounding is not found, search the EpiDKG index
		if (grounding == null) {
			grounding = new Grounding();
			List<DKG> dkgResults;

			try {
				dkgResults = dkgService.searchEpiDKG(page, pageSize, term, null);
			} catch (Exception e) {
				log.error("Error searching assets", e);
				return ResponseEntity.internalServerError().build();
			}

			// Transform the DKG results into a Grounding.identifiers
			final Map<String, String> identifiers = new HashMap<>();
			dkgResults.forEach(dkg -> {
				String[] parts = dkg.getCurie().split(":");
				if (parts.length == 2) {
					identifiers.put(parts[0], parts[1]);
				}
			});
			grounding.setIdentifiers(identifiers);
		}

		return ResponseEntity.ok(grounding);
	}

	@GetMapping("/search/embeddings")
	@Secured(Roles.USER)
	@Operation(summary = "Search for DKG entities using embeddings in the EpiDKG index")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Search results",
				content = @Content(
					array = @io.swagger.v3.oas.annotations.media.ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DKG.class)
					)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue with the search", content = @Content)
		}
	)
	public ResponseEntity<List<DKG>> knnSearchAssets(
		@RequestParam(required = false, defaultValue = "0") final Integer page,
		@RequestParam(required = false, defaultValue = "10") final Integer pageSize,
		@RequestParam(required = false, defaultValue = "10") final Integer k,
		@RequestParam final String text
	) {
		try {
			return ResponseEntity.ok(dkgService.knnSearchEpiDKG(page, pageSize, k, List.of(text), null));
		} catch (Exception e) {
			log.error("Error searching assets", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/currie/{curie}")
	@Secured(Roles.USER)
	@Operation(summary = "Search for DKG entities using the id in the EpiDKG index")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Search results",
				content = @Content(
					array = @io.swagger.v3.oas.annotations.media.ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DKG.class)
					)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue with the search", content = @Content)
		}
	)
	public ResponseEntity<List<DKG>> getEntity(@PathVariable("curie") final String curie) {
		try {
			return ResponseEntity.ok(dkgService.getEpiEntity(curie));
		} catch (Exception e) {
			log.error("Error getting entity", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
