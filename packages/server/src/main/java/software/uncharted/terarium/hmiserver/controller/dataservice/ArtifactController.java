package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
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
			return ResponseEntity.status(HttpStatus.CREATED).body(artifactService.createAsset(artifact, projectId));
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
	public ResponseEntity<Artifact> getArtifact(
		@PathVariable("id") final UUID artifactId,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);
		try {
			final Optional<Artifact> artifact = artifactService.getAsset(artifactId);
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
		@RequestBody final Artifact artifact,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			artifact.setId(artifactId);
			final Optional<Artifact> updated = artifactService.updateAsset(artifact, projectId);
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
	public ResponseEntity<ResponseDeleted> deleteArtifact(
		@PathVariable("id") final UUID artifactId,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			artifactService.deleteAsset(artifactId, projectId);
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
}
