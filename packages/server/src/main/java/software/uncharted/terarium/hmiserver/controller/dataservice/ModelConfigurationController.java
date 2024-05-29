package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.utils.Messages;

@RequestMapping("/model-configurations")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ModelConfigurationController {
	final ModelConfigurationService modelConfigurationService;
	final CurrentUserService currentUserService;
	final Messages messages;

	/**
	 * Gets all model configurations (which are visible to this user)
	 *
	 * @param pageSize how many entries per page
	 * @param page page number
	 * @return all model configurations visible to this user
	 */
	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations (which are visible to this user)")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "model configurations found.",
						content =
								@Content(
										array =
												@ArraySchema(
														schema =
																@io.swagger.v3.oas.annotations.media.Schema(
																		implementation = Project.class)))),
				@ApiResponse(
						responseCode = "204",
						description = "There are no errors, but also no model configurations for this user",
						content = @Content),
				@ApiResponse(
						responseCode = "503",
						description = "There was an issue communicating with back-end services",
						content = @Content)
			})
	public ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
			@RequestParam(name = "page-size", defaultValue = "500") final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "1") final Integer page) {

		try {
			final List<ModelConfiguration> modelConfigurations =
					modelConfigurationService.getPublicNotTemporaryAssets(pageSize, page);
			if (modelConfigurations.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(modelConfigurations);
		} catch (final Exception e) {
			log.error("Unable to get model configurations from postgres db", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
					messages.get("postgres.service-unavailable"));
		}
	}
}
