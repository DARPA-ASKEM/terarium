package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;

@RequestMapping("/artifacts")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ArtifactController {

	final ArtifactService artifactService;

	final JsDelivrProxy gitHubProxy;

	final ObjectMapper objectMapper;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<Artifact>> getArtifacts(
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) throws IOException {
		return ResponseEntity.ok(artifactService.getArtifacts(pageSize, page));
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<Artifact> createArtifact(@RequestBody Artifact artifact) throws IOException {
		return ResponseEntity.status(HttpStatus.CREATED).body(artifactService.createArtifact(artifact));
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Artifact> getArtifact(@PathVariable("id") UUID artifactId) throws IOException {
		Optional<Artifact> artifact = artifactService.getArtifact(artifactId);
		if (artifact.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(artifact.get());
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Artifact> updateArtifact(
			@PathVariable("id") UUID artifactId,
			@RequestBody Artifact artifact) throws IOException {

		final Optional<Artifact> updated = artifactService.updateArtifact(artifact.setId(artifactId));
		if (updated.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(updated.get());
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> deleteArtifact(@PathVariable("id") UUID artifactId) throws IOException {
		artifactService.deleteArtifact(artifactId);

		JsonNode res = objectMapper
				.valueToTree(Map.of("message", String.format("Artifact successfully deleted: %s", artifactId)));

		return ResponseEntity.ok(res);
	}

	@GetMapping("/{id}/upload-url")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a presigned url to download the document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Presigned url generated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PresignedURL.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the presigned url", content = @Content)
	})
	public ResponseEntity<PresignedURL> getUploadURL(
			@PathVariable("id") final UUID id,
			@PathVariable("filename") final String filename) {

		try {
			return ResponseEntity.ok(artifactService.getUploadUrl(id, filename));
		} catch (Exception e) {
			final String error = "Unable to get upload url";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/download-url")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a presigned url to upload the document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Presigned url generated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PresignedURL.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the presigned url", content = @Content)
	})
	public ResponseEntity<PresignedURL> getDownloadURL(
			@PathVariable("id") final UUID id,
			@PathVariable("filename") final String filename) {

		try {
			return ResponseEntity.ok(artifactService.getDownloadUrl(id, filename));
		} catch (Exception e) {
			final String error = "Unable to get download url";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/download-file-as-text")
	@Secured(Roles.USER)
	public ResponseEntity<String> downloadFileAsText(@PathVariable("id") UUID artifactId,
			@RequestParam("filename") String filename) {

		try (CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final PresignedURL presignedURL = artifactService.getDownloadUrl(artifactId, filename);
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return ResponseEntity.ok(textFileAsString);

		} catch (Exception e) {
			log.error("Unable to GET file as string data", e);
			return ResponseEntity.internalServerError().build();
		}

	}

	@GetMapping("/{id}/download-file")
	@Secured(Roles.USER)
	public ResponseEntity<byte[]> downloadFile(@PathVariable("id") UUID artifactId,
			@RequestParam("filename") String filename) {

		log.debug("Downloading artifact {} from project", artifactId);

		try (CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final PresignedURL presignedURL = artifactService.getDownloadUrl(artifactId, filename);
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
				byte[] fileAsBytes = response.getEntity().getContent().readAllBytes();
				return ResponseEntity.ok(fileAsBytes);
			}
			return ResponseEntity.status(response.getStatusLine().getStatusCode()).build();

		} catch (Exception e) {
			log.error("Unable to GET artifact data", e);
			return ResponseEntity.internalServerError().build();
		}

	}

	@PutMapping(value = "/{artifactId}/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<Integer> uploadFile(
			@PathVariable("artifactId") final UUID artifactId,
			@RequestParam("filename") final String filename,
			@RequestPart("file") MultipartFile input) throws IOException {

		log.debug("Uploading artifact {} to project", artifactId);

		byte[] fileAsBytes = input.getBytes();
		HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		return uploadArtifactHelper(artifactId, filename, fileEntity);

	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it
	 * to the project.
	 */
	@PutMapping("/{artifactId}/uploadArtifactFromGithub")
	@Secured(Roles.USER)
	public ResponseEntity<Integer> uploadArtifactFromGithub(
			@PathVariable("artifactId") final UUID artifactId,
			@RequestParam("path") final String path,
			@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
			@RequestParam("filename") final String filename) {
		log.debug("Uploading artifact file from github to dataset {}", artifactId);

		// download file from GitHub
		String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path).getBody();
		HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
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
	private ResponseEntity<Integer> uploadArtifactHelper(UUID artifactId, String fileName,
			HttpEntity artifactHttpEntity) {

		try (CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			// upload file to S3
			final PresignedURL presignedURL = artifactService.getUploadUrl(artifactId, fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(artifactHttpEntity);
			final HttpResponse response = httpclient.execute(put);
			;

			return ResponseEntity.ok(response.getStatusLine().getStatusCode());

		} catch (Exception e) {
			log.error("Unable to PUT artifact data", e);
			return ResponseEntity.internalServerError().build();
		}
	}

}
