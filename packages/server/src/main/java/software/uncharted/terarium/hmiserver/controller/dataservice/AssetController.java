package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacProject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

@RequestMapping("/assets")
@RestController
@Slf4j
@RequiredArgsConstructor
@Transactional
@Tags(@Tag(name = "Assets", description = "Asset related operations"))
public class AssetController {

	final ProjectService projectService;
	final ProjectAssetService projectAssetService;
	final ReBACService reBACService;
	final CurrentUserService currentUserService;

	/**
	 * Check if an asset name is available for a given asset type. If a ProjectId is
	 * given the search will be
	 * limited to just that project. Otherwise, the entire asset type will be
	 * searched. If the asset name is available,
	 * a 204 No Content response is returned. If the asset name is not available, a
	 * 409 Conflict response is returned.
	 * 
	 * @param assetType Asset type to check
	 * @param assetName Asset name to check
	 * @param projectId Project ID to limit the search to (optional)
	 * @return 204 No Content if the asset name is available, 409 Conflict if the
	 *         asset name is not available
	 */
	@GetMapping("/asset-name-available/{asset-type}/{asset-name}")
	@Secured(Roles.USER)
	@Operation(summary = "Check if an asset name is available for a given asset type. If a ProjectId is given the search will be limited to just that project. Otherwise, the entire asset type will be searched.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Asset name is available"),
			@ApiResponse(responseCode = "409", description = "Asset name is not available"),
			@ApiResponse(responseCode = "404", description = "Project id provided, but project not found"),
			@ApiResponse(responseCode = "403", description = "User does not have permission to access this project"),
			@ApiResponse(responseCode = "500", description = "Unable to verify project permissions")
	})
	public ResponseEntity<Void> verifyAssetNameAvailability(
			@PathVariable("asset-type") AssetType assetType,
			@PathVariable("asset-name") String assetName,
			@RequestParam(name = "project-id", required = false) UUID projectId) {

		if (projectId == null) {

			final Optional<ProjectAsset> asset = projectAssetService.getProjectAssetByNameAndType(assetName, assetType);
			if (asset.isPresent()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset name is not available");
			} else {
				return ResponseEntity.noContent().build();
			}

		} else {
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(projectId, reBACService);
			try {
				if (rebacUser.canRead(rebacProject)) {
					final Optional<Project> project = projectService.getProject(projectId);
					if (project.isPresent()) {
						final Optional<ProjectAsset> asset = projectAssetService
								.getProjectAssetByNameAndTypeAndProjectId(projectId, assetName, assetType);
						if (asset.isPresent()) {
							throw new ResponseStatusException(HttpStatus.CONFLICT,
									"Asset name is not available in this project");
						} else {
							return ResponseEntity.noContent().build();
						}
					} else {
						throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
					}
				} else {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN,
							"User does not have permission to access this project");
				}
			} catch (ResponseStatusException e) {
				throw e; // Like any responsible fisher, we're going to catch and release!
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to verify project permissions");
			}
		}

	}

}
