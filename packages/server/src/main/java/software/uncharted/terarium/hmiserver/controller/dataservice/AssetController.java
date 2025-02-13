package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/assets")
@RestController
@Slf4j
@RequiredArgsConstructor
@Tags(@Tag(name = "Assets", description = "Asset related operations"))
public class AssetController {

	final ProjectService projectService;
	final ProjectAssetService projectAssetService;
	final ReBACService reBACService;
	final CurrentUserService currentUserService;
	final ObjectMapper objectMapper;

	/**
	 * Check if an asset name is available for a given asset type. If a ProjectId is given the search will be limited to
	 * just that project. Otherwise, the entire asset type will be searched. If the asset name is available, a 204 No
	 * Content response is returned. If the asset name is not available, a 409 Conflict response is returned.
	 *
	 * @param assetTypeName Asset type to check
	 * @param assetName Asset name to check
	 * @param projectId Project ID to limit the search to (optional)
	 * @return 204 No Content if the asset name is available, 409 Conflict if the asset name is not available
	 */
	@GetMapping("/asset-name-available/{asset-type}/{asset-name}")
	@Secured(Roles.USER)
	@Operation(
		summary = "Check if an asset name is available for a given asset type. If a ProjectId is given the search will be limited to just that project. Otherwise, the entire asset type will be searched."
	)
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "204", description = "Asset name is available"),
			@ApiResponse(responseCode = "409", description = "Asset name is not available"),
			@ApiResponse(responseCode = "404", description = "Project id provided, but project not found"),
			@ApiResponse(responseCode = "403", description = "User does not have permission to access this project"),
			@ApiResponse(responseCode = "500", description = "Unable to verify project permissions")
		}
	)
	public ResponseEntity<Void> verifyAssetNameAvailability(
		@PathVariable("asset-type") final String assetTypeName,
		@PathVariable("asset-name") final String assetName,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission assumedPermission = Schema.Permission.READ;
		final AssetType assetType = AssetType.getAssetType(assetTypeName, objectMapper);

		if (projectId == null) {
			final Optional<ProjectAsset> asset = projectAssetService.getProjectAssetByNameAndType(
				assetName,
				assetType,
				assumedPermission
			);
			if (asset.isPresent()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset name is already in use");
			} else {
				return ResponseEntity.noContent().build();
			}
		} else {
			try {
				final Optional<Project> project = projectService.getProject(projectId);
				if (project.isPresent()) {
					final Optional<ProjectAsset> asset = projectAssetService.getProjectAssetByNameAndTypeAndProjectId(
						projectId,
						assetName,
						assetType,
						assumedPermission
					);
					if (asset.isPresent()) {
						throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset name is not available in this project");
					} else {
						return ResponseEntity.noContent().build();
					}
				} else {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
				}
			} catch (final ResponseStatusException e) {
				throw e; // Like any responsible fisher, we're going to catch and release!
			}
		}
	}
}
