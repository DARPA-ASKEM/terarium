package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.proxies.github.GithubProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.CodeService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RequestMapping("/code-asset")
@RestController
@Slf4j
@RequiredArgsConstructor
public class TDSCodeController{

	final JsDelivrProxy jsdelivrProxy;

	final GithubProxy githubProxy;

	final CodeService codeService;

	final ObjectMapper objectMapper;

	/**
	 * Retrieves a list of codes.
	 *
	 * @param pageSize The number of codes to retrieve per page (optional, default value is 100).
	 * @param page The page number to retrieve (optional, default value is 0).
	 * @return A ResponseEntity containing a list of Code objects if successful, or an empty list if no codes are found.
	 */
	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all code resources")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "publications found.",
			content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Code.class)))),

		@ApiResponse(
			responseCode = "204",
			description = "There are no publications found and no errors occurred",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving publications from the data store",
			content = @Content
		)
	})
	public ResponseEntity<List<Code>> getCodes(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {
        try {
            return ResponseEntity.ok(codeService.getCode(pageSize, page));
        } catch (IOException e) {
					log.error("Unable to get code resources", e);
					throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to get code resources"
					);
        }
    }

	/**
	 * Creates a code.
	 *
	 * @param code The code to be created.
	 * @return A ResponseEntity containing the created code's ID in JSON format.
	 */
	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new code resource")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Code resource created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Code.class))),
		@ApiResponse(responseCode = "500", description = "There was an issue creating the code resource", content = @Content)
	})
	public ResponseEntity<Code> createCode(@RequestBody Code code){

		try {
				codeService.createCode(code);
		} catch (IOException e) {
			log.error("Unable to create code resource", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to create code resource"
			);
		}

		return ResponseEntity.ok(code);
	}

	/**
	 * Retrieves the code with the specified ID.
	 *
	 * @param id the ID of the code to be retrieved
	 * @return a ResponseEntity containing the code
	 * @throws ResponseStatusException if the code is not found with the specified ID
	 */
	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets code resource by ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Code resource found.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Code.class))
		),
		@ApiResponse(responseCode = "404",
			description = "There was no code resource found but no errors occurred",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving the code resource from the data store",
			content = @Content
		)
	})
	public ResponseEntity<Code> getCode(@PathVariable("id") UUID id) {

		final Code code;
		try {
				code = codeService.getCode(id);
		} catch (IOException e) {
			log.error("Unable to get code resource", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to get code resource"
			);
		}
		if (code == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Code %s not found", id));
		}

		// Return the updated document
		return ResponseEntity.ok(code);
	}

	/**
	 * Updates the code with the specified ID.
	 *
	 * @param codeId The ID of the code to update.
	 * @param code The updated code information.
	 * @return The HTTP response entity containing a JSON node with the updated code ID.
	 */
	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a code resource")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Code resource updated.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Code.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue updating the code resource",
			content = @Content
		)
	})
	public ResponseEntity<Code> updateCode(@PathVariable("id") UUID codeId, @RequestBody Code code){

		code.setId(codeId);

		try {
				codeService.updateCode(code);
		} catch (IOException e) {
			log.error("Unable to update code resource", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to update code resource"
			);
		}

		return ResponseEntity.ok(code);
	}

	/**
	 * Deletes a code with the given ID.
	 *
	 * @param id The ID of the code to delete.
	 * @return A ResponseEntity containing a JsonNode object with a success message.
	 * @throws IOException if an error occurs while deleting the code.
	 */
	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Delete a code resource by ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Code resource deleted.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue deleting the code resource",
			content = @Content
		)
	})
	public ResponseEntity<ResponseDeleted> deleteCode(@PathVariable("id") UUID id){

		try {
				codeService.deleteCode(id);
		} catch (IOException e) {
			log.error("Unable to delete code resource", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to delete code resource"
			);
		}


		return ResponseEntity.ok(new ResponseDeleted("Code", id));
	}

	/**
	 * Retrieves the content of a code file as text.
	 *
	 * @param codeId    the ID of the code file to be retrieved
	 * @param filename  the name of the code file
	 * @return a ResponseEntity object containing the content of the code file as text
	 */
	@GetMapping("/{id}/download-code-as-text")
	@Secured(Roles.USER)
	@Operation(summary = "Gets code file as text")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Code file found.",
			content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving the code file from the data store",
			content = @Content
		)
	})
	public ResponseEntity<String> getCodeFileAsText(@PathVariable("id") UUID codeId, @RequestParam("filename") String filename) {

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			final PresignedURL presignedURL = codeService.getDownloadUrl(codeId, filename);
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return ResponseEntity.ok(textFileAsString);

		} catch (Exception e) {
			log.error("Unable to GET file as string data", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to get file as string data"
			);
		}

	}

	/**
	 * Uploads a file to the specified codeId.
	 *
	 * @param codeId   the code ID to upload the file to
	 * @param filename the name of the file to be uploaded
	 * @param input    the file to be uploaded
	 * @return a ResponseEntity object with an Integer indicating the result of the upload
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	@PutMapping(value = "/{codeId}/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a file to the specified codeId")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "File uploaded.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Integer.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue uploading the file",
			content = @Content
		)
	})
	public ResponseEntity<Integer> uploadFile(
		@PathVariable("codeId") final UUID codeId,
		@RequestParam("filename") final String filename,
		@RequestPart("file") MultipartFile input
	) {

		log.debug("Uploading code {} to project", codeId);

		byte[] fileAsBytes = new byte[0];
		try {
				fileAsBytes = input.getBytes();
		} catch (IOException e) {
			log.error("Unable to read file", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to read file"
			);
		}
		HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		return uploadCodeHelper(codeId, filename, fileEntity);


	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it to the project.
	 */
	@PutMapping("/{codeId}/uploadCodeFromGithub")
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a file from GitHub given the path and owner name, then uploads it to the project")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "File uploaded.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Integer.class))
		)
	})
	public ResponseEntity<Integer> uploadCodeFromGithub(
		@PathVariable("codeId") final UUID codeId,
		@RequestParam("path") final String path,
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("filename") final String filename
	) {
		log.debug("Uploading code file from github to dataset {}", codeId);

		//download file from GitHub
		String fileString = jsdelivrProxy.getGithubCode(repoOwnerAndName, path).getBody();
		HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadCodeHelper(codeId, filename, fileEntity);

	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it to the project.
	 * @param codeId The ID of the code to upload to
	 * @param repoOwnerAndName The owner and name of the repo to upload from
	 * @param repoName The name of the repo to upload from
	 * @return A response containing the status of the upload
	 */
	@PutMapping("/{codeId}/uploadCodeFromGithubRepo")
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a file from GitHub given the path and owner name, then uploads it to the project")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "File uploaded.",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Integer.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue uploading the file",
			content = @Content
		)
	})
	public ResponseEntity<Integer> uploadCodeFromGithub(
		@PathVariable("codeId") final UUID codeId,
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("repoName") final String repoName
	) {
		try(final CloseableHttpClient httpClient = HttpClients.custom()
			.build()) {

			String githubApiUrl = "https://api.github.com/repos/" + repoOwnerAndName + "/zipball/";

			// get github repo zip
			HttpGet httpGet = new HttpGet(githubApiUrl);
            HttpResponse response = httpClient.execute(httpGet);
			final byte[] zipBytes = response.getEntity().getContent().readAllBytes();

			HttpEntity fileEntity = new ByteArrayEntity(zipBytes, ContentType.APPLICATION_OCTET_STREAM);

			return uploadCodeHelper(codeId, repoName, fileEntity);

		} catch (Exception e){
			log.error("Unable to GET file as string data", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to get file as string data"
			);
		}

	}
	/**
	 * Uploads an code inside the entity to TDS via a presigned URL
	 *
	 * @param codeId         The ID of the code to upload to
	 * @param fileName       The name of the file to upload
	 * @param codeHttpEntity The entity containing the code to upload
	 * @return A response containing the status of the upload
	 */
	private ResponseEntity<Integer> uploadCodeHelper(UUID codeId, String fileName, HttpEntity codeHttpEntity) {

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = codeService.getUploadUrl(codeId, fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(codeHttpEntity);
			final HttpResponse response = httpclient.execute(put);

			return ResponseEntity.ok(response.getStatusLine().getStatusCode());

		} catch (Exception e) {
			log.error("Unable to PUT artifact data", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
