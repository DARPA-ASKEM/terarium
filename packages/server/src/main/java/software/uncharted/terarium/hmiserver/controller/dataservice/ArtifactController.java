package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.controller.mira.MiraController;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.service.tasks.MdlToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.SbmlToPetrinetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.StellaToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/artifacts")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ArtifactController {

	final Config config;

	final ArtifactService artifactService;

	final JsDelivrProxy gitHubProxy;

	final ObjectMapper objectMapper;

	private final ProjectService projectService;
	private final CurrentUserService currentUserService;
	private final Messages messages;
	private final ModelConfigurationService modelConfigurationService;
	private final TaskService taskService;
	final ProjectAssetService projectAssetService;
	final ProvenanceService provenanceService;

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Creates a new artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Artifact created.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Artifact.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the artifact", content = @Content)
		}
	)
	public ResponseEntity<Artifact> createArtifact(
		@RequestBody final Artifact artifact,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(
				artifactService.createAsset(artifact, projectId, permission)
			);
		} catch (final Exception e) {
			final String error = "An error occurred while creating artifact";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets an artifact by ID")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Artifact retrieved.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Artifact.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Artifact not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the artifact", content = @Content)
		}
	)
	public ResponseEntity<Artifact> getArtifact(@PathVariable("id") final UUID artifactId) {
		final UUID projectId = artifactService.getProjectIdForAsset(artifactId);
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);
		try {
			final Optional<Artifact> artifact = artifactService.getAsset(artifactId, permission);
			return artifact.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final Exception e) {
			final String error = "An error occurred while retrieving artifact";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Updates an artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Artifact updated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Artifact.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Artifact not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the artifact", content = @Content)
		}
	)
	public ResponseEntity<Artifact> updateArtifact(
		@PathVariable("id") final UUID artifactId,
		@RequestBody final Artifact artifact
	) {
		final UUID projectId = artifactService.getProjectIdForAsset(artifactId);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			artifact.setId(artifactId);
			final Optional<Artifact> updated = artifactService.updateAsset(artifact, projectId, permission);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final Exception e) {
			final String error = "An error occurred while updating artifact";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Artifact deleted.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue deleting the artifact", content = @Content)
		}
	)
	public ResponseEntity<ResponseDeleted> deleteArtifact(@PathVariable("id") final UUID artifactId) {
		final UUID projectId = artifactService.getProjectIdForAsset(artifactId);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			artifactService.deleteAsset(artifactId, projectId, permission);
			return ResponseEntity.ok(new ResponseDeleted("artifact", artifactId));
		} catch (final Exception e) {
			final String error = "Unable to delete artifact";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/upload-url")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a presigned url to upload the document")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Presigned url generated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PresignedURL.class)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the presigned url",
				content = @Content
			)
		}
	)
	public ResponseEntity<PresignedURL> getUploadURL(
		@PathVariable("id") final UUID id,
		@RequestParam("filename") final String filename
	) {
		try {
			return ResponseEntity.ok(artifactService.getUploadUrl(id, filename));
		} catch (final Exception e) {
			final String error = "Unable to get upload url";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/download-url")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a presigned url to download the document")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Presigned url generated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PresignedURL.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Presigned url not found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the presigned url",
				content = @Content
			)
		}
	)
	public ResponseEntity<PresignedURL> getDownloadURL(
		@PathVariable("id") final UUID id,
		@RequestParam("filename") final String filename
	) {
		try {
			final Optional<PresignedURL> url = artifactService.getDownloadUrl(id, filename);
			return url.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final Exception e) {
			final String error = "Unable to get download url";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/download-file-as-text")
	@Secured(Roles.USER)
	@Operation(summary = "Downloads a file from the artifact as a string")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "File downloaded.",
				content = @Content(mediaType = "text/plain")
			),
			@ApiResponse(responseCode = "500", description = "There was an issue downloading the file", content = @Content)
		}
	)
	public ResponseEntity<String> downloadFileAsText(
		@PathVariable("id") final UUID artifactId,
		@RequestParam("filename") final String filename
	) {
		try {
			final Optional<String> textFileAsString = artifactService.fetchFileAsString(artifactId, filename);
			return textFileAsString.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final Exception e) {
			log.error("Unable to GET file as string data", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to GET file as string data"
			);
		}
	}

	@GetMapping("/{id}/download-file")
	@Secured(Roles.USER)
	@Operation(summary = "Downloads a file from the artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "File downloaded.",
				content = @Content(mediaType = "application/octet-stream")
			),
			@ApiResponse(responseCode = "500", description = "There was an issue downloading the file", content = @Content)
		}
	)
	public ResponseEntity<byte[]> downloadFile(
		@PathVariable("id") final UUID artifactId,
		@RequestParam("filename") final String filename
	) {
		log.debug("Downloading artifact {} from project", artifactId);

		try {
			final Optional<byte[]> bytes = artifactService.fetchFileAsBytes(artifactId, filename);

			if (bytes.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			final CacheControl cacheControl = CacheControl.maxAge(
				config.getCacheHeadersMaxAge(),
				TimeUnit.SECONDS
			).cachePublic();
			return ResponseEntity.ok().cacheControl(cacheControl).body(bytes.get());
		} catch (final Exception e) {
			log.error("Unable to GET artifact data", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to GET artifact data"
			);
		}
	}

	@PutMapping(value = "/{id}/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a file to the artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "File uploaded.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Integer.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the file", content = @Content)
		}
	)
	public ResponseEntity<Integer> uploadFile(
		@PathVariable("id") final UUID artifactId,
		@RequestParam("filename") final String filename,
		@RequestPart("file") final MultipartFile input
	) throws IOException {
		log.debug("Uploading artifact {} to project", artifactId);

		final byte[] fileAsBytes = input.getBytes();
		final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		return uploadArtifactHelper(artifactId, filename, fileEntity);
	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it
	 * to the project.
	 */
	@PutMapping("/{id}/upload-artifact-from-github")
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a file from GitHub to the artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "File uploaded.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Integer.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the file", content = @Content)
		}
	)
	public ResponseEntity<Integer> uploadArtifactFromGithub(
		@PathVariable("id") final UUID artifactId,
		@RequestParam("path") final String path,
		@RequestParam("repo-owner-and-name") final String repoOwnerAndName,
		@RequestParam("filename") final String filename
	) {
		log.debug("Uploading artifact file from github to dataset {}", artifactId);

		// download file from GitHub
		final String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path).getBody();
		if (fileString == null) {
			return ResponseEntity.notFound().build();
		}
		final HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadArtifactHelper(artifactId, filename, fileEntity);
	}

	/**
	 * Uploads an artifact inside the entity to TDS via a presigned URL
	 *
	 * @param artifactId         The ID of the artifact to upload to
	 * @param fileName           The name of the file to upload
	 * @param artifactHttpEntity The entity containing the artifact to upload
	 * @return A response containing the status of the upload
	 */
	private ResponseEntity<Integer> uploadArtifactHelper(
		final UUID artifactId,
		final String fileName,
		final HttpEntity artifactHttpEntity
	) {
		try {
			// upload file to S3
			final Integer status = artifactService.uploadFile(artifactId, fileName, artifactHttpEntity);
			return ResponseEntity.ok(status);
		} catch (final IOException e) {
			log.error("Unable to upload artifact data", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to PUT artifact data"
			);
		}
	}

	@PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a file to the artifact")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "File uploaded.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Integer.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the file", content = @Content)
		}
	)
	public ResponseEntity<ProjectAsset> uploadFileForModel(
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestParam("filename") final String filename,
		@RequestPart("file") final MultipartFile input
	) throws IOException {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		if (!endsWith(filename, List.of(".mdl", ".xmile", ".itmx", ".stmx", ".sbml", ".xml"))) {
			log.error(String.format("Unable to determine the artifact type from the supplied filename: %s", filename));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("artifact.file-type"));
		}

		final List<String> filenames = new ArrayList<>();
		filenames.add(filename);
		final Artifact tempArtifact = new Artifact();
		tempArtifact.setUserId(currentUserService.get().getId()).setFileNames(filenames);
		Artifact createdArtifact = null;

		try {
			createdArtifact = artifactService.createAsset(tempArtifact, projectId, permission);
		} catch (final IOException e) {
			final String error = "Unable to create artifact for model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		try {
			log.debug("Uploading artifact {} to project", createdArtifact.getId());

			final byte[] fileAsBytes = input.getBytes();
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			uploadArtifactHelper(createdArtifact.getId(), filename, fileEntity);
		} catch (final IOException e) {
			final String error = "Unable to upload artifact for model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		final Optional<String> fileContents;
		try {
			fileContents = artifactService.fetchFileAsString(createdArtifact.getId(), filename);
		} catch (final IOException e) {
			log.error(String.format("Unable to read file contents for artifact %s.", createdArtifact.getId()), e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("artifact.file.unable-to-read"));
		}

		if (fileContents.isEmpty()) {
			log.error(String.format("The file contents for artifact %s is empty.", createdArtifact.getId()));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("artifact.file.content.empty"));
		}

		final MiraController.ConversionAdditionalProperties additionalProperties =
			new MiraController.ConversionAdditionalProperties();
		additionalProperties.setProjectId(projectId);
		additionalProperties.setFileName(filename);

		final TaskRequest req = new TaskRequest();
		req.setType(TaskRequest.TaskType.MIRA);

		try {
			req.setInput(fileContents.get().getBytes());
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}
		req.setAdditionalProperties(additionalProperties);
		req.setUserId(currentUserService.get().getId());

		if (endsWith(filename, List.of(".mdl"))) {
			req.setScript(MdlToStockflowResponseHandler.NAME);
		} else if (endsWith(filename, List.of(".xmile", ".itmx", ".stmx"))) {
			req.setScript(StellaToStockflowResponseHandler.NAME);
		} else if (endsWith(filename, List.of(".sbml", ".xml"))) {
			req.setScript(SbmlToPetrinetResponseHandler.NAME);
		} else {
			log.error(String.format("Unable to determine the artifact type from the supplied filename: %s", filename));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("artifact.file-type"));
		}

		// send the request
		final TaskResponse resp;
		try {
			resp = taskService.runTaskSync(req);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		}

		final Model model;
		try {
			model = objectMapper.readValue(resp.getOutput(), Model.class);
			// create a default configuration
			final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
				model,
				null,
				null
			);
			modelConfigurationService.createAsset(modelConfiguration, projectId, permission);
		} catch (final IOException e) {
			log.error("Unable to deserialize output", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}

		final AssetType assetType = AssetType.MODEL;
		final ProjectAsset projectAsset = projectAssetService.createProjectAsset(projectId, assetType, model, permission);

		final Provenance provenance = new Provenance()
			.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
			.setLeft(model.getId())
			.setLeftType(ProvenanceType.MODEL)
			.setRight(createdArtifact.getId())
			.setRightType(ProvenanceType.ARTIFACT);
		provenanceService.createProvenance(provenance);

		return ResponseEntity.status(HttpStatus.CREATED).body(projectAsset);
	}

	private static boolean endsWith(final String filename, final List<String> suffixes) {
		for (final String suffix : suffixes) {
			if (filename.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}
}
