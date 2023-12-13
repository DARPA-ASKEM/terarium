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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.UserId;
import software.uncharted.terarium.hmiserver.models.data.project.Project;
import software.uncharted.terarium.hmiserver.models.data.project.ResourceType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseStatus;
import software.uncharted.terarium.hmiserver.models.dataservice.document.AddDocumentAssetFromXDDRequest;
import software.uncharted.terarium.hmiserver.models.dataservice.document.AddDocumentAssetFromXDDResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequestMapping("/document-asset")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DocumentController {

	final ExtractionProxy extractionProxy;

	final SkemaUnifiedProxy skemaUnifiedProxy;

	final JsDelivrProxy gitHubProxy;

	final DownloadService downloadService;

	final KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

	private final ProjectService projectService;
	private final ProjectAssetService projectAssetService;

	final DocumentAssetService documentAssetService;

	final ObjectMapper objectMapper;

	@Value("${xdd.api-key}")
	String apikey;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all documents")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Documents found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DocumentAsset.class)))),
			@ApiResponse(responseCode = "204", description = "There are no documents found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving documents from the data store", content = @Content)
	})
	public ResponseEntity<List<DocumentAsset>> getDocuments(
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {
		try {
			return ResponseEntity.ok(documentAssetService.getDocumentAssets(page, pageSize));
		} catch (final IOException e) {
			final String error = "Unable to get documents";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the document", content = @Content)
	})
	public ResponseEntity<ResponseId> createDocument(
			@RequestBody final DocumentAsset document) {

		try {
			documentAssetService.createDocumentAsset(document);
			return ResponseEntity.ok(new ResponseId(document.getId()));
		} catch (final IOException e) {
			final String error = "Unable to create document";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets document by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DocumentAsset.class))),
			@ApiResponse(responseCode = "204", description = "There was no document found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the document from the data store", content = @Content)
	})
	public ResponseEntity<DocumentAsset> getDocument(
			@PathVariable("id") final UUID id) {

		try {
			final DocumentAsset document = documentAssetService.getDocumentAsset(id);
			if (document == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Document %s not found", id));
			}

			// Test if the document as any assets
			if (document.getAssets() == null) {
				return ResponseEntity.ok(document);
			}

			document.getAssets().forEach(asset -> {
				try {
					// Add the S3 bucket url to each asset metadata
					final String url = documentAssetService.getDownloadUrl(id, asset.getFileName()).getUrl();
					asset.getMetadata().put("url", url);

					// if the asset os of type equation
					if (asset.getAssetType().equals(ExtractionAssetType.EQUATION)
							&& asset.getMetadata().get("equation") == null) {
						// Fetch the image from the URL
						final byte[] imagesByte = IOUtils.toByteArray(new URL(url));
						// Encode the image in Base 64
						final String imageB64 = Base64.getEncoder().encodeToString(imagesByte);

						// Send it to SKEMA to get the Presentation MathML equation
						final String equation = skemaUnifiedProxy.postImageToEquations(imageB64).getBody();

						log.warn("Equation: {}", equation);

						// Add the equations into the metadata
						asset.getMetadata().put("equation", equation);
					}
				} catch (final Exception e) {
					log.error("Unable to extract S3 url for assets or extract equations", e);
				}
			});

			// Update data-service with the updated metadata
			documentAssetService.updateDocumentAsset(document);

			// Return the updated document
			return ResponseEntity.ok(document);
		} catch (final IOException e) {
			final String error = "Unable to get document";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
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
			return ResponseEntity.ok(documentAssetService.getUploadUrl(id, filename));
		} catch (final Exception e) {
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
			return ResponseEntity.ok(documentAssetService.getDownloadUrl(id, filename));
		} catch (final Exception e) {
			final String error = "Unable to get download url";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete document", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Document could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	public ResponseEntity<ResponseDeleted> deleteDocument(
			@PathVariable("id") final UUID id) {

		try {
			documentAssetService.deleteDocumentAsset(id);
			return ResponseEntity.ok(new ResponseDeleted("Document", id));
		} catch (final IOException e) {
			final String error = "Unable to delete document";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Uploads an artifact inside the entity to TDS via a presigned URL
	 *
	 * @param documentId The ID of the document to upload to
	 * @param fileName   The name of the file to upload
	 * @param fileEntity The entity containing the file to upload
	 * @return A response containing the status of the upload
	 */
	private ResponseEntity<ResponseStatus> uploadDocumentHelper(final UUID documentId, final String fileName,
			final HttpEntity fileEntity) {
		try (final CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			// upload file to S3
			final PresignedURL presignedURL = documentAssetService.getUploadUrl(documentId, fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpclient.execute(put);

			// if the fileEntity is not a PDF, then we need to extract the text and update
			// the document asset
			if (!DownloadService.IsPdf(fileEntity.getContent().readAllBytes())) {
				final DocumentAsset document = documentAssetService.getDocumentAsset(documentId);
				if (document == null) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND,
							String.format("Document %s not found", documentId));
				}

				document.setText(IOUtils.toString(fileEntity.getContent(), StandardCharsets.UTF_8));

				documentAssetService.updateDocumentAsset(document);
			}
			return ResponseEntity.ok(new ResponseStatus(response.getStatusLine().getStatusCode()));

		} catch (final IOException e) {
			final String error = "Unable to upload document";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	/**
	 * Uploads a file to the project.
	 */
	@PutMapping(value = "/{id}/uploadDocument", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a document to a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Uploaded the document.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the document", content = @Content)
	})
	public ResponseEntity<ResponseStatus> uploadDocument(
			@PathVariable("id") final UUID id,
			@RequestParam("filename") final String filename,
			@RequestPart("file") final MultipartFile file) {

		try {
			final byte[] fileAsBytes = file.getBytes();
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			return uploadDocumentHelper(id, filename, fileEntity);
		} catch (final IOException e) {
			final String error = "Unable to upload document";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it
	 * to the project.
	 */
	@PutMapping("/{documentId}/uploadDocumentFromGithub")
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a document from github to a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Uploaded the document.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the document", content = @Content)
	})
	public ResponseEntity<ResponseStatus> uploadDocumentFromGithub(
			@PathVariable("documentId") final UUID documentId,
			@RequestParam("path") final String path,
			@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
			@RequestParam("filename") final String filename) {

		log.debug("Uploading Document file from github to dataset {}", documentId);

		// download file from GitHub
		final String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path).getBody();
		final HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadDocumentHelper(documentId, filename, fileEntity);
	}

	@PostMapping(value = "/createDocumentFromXDD")
	@Secured(Roles.USER)
	@Operation(summary = "Creates a document from XDD")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Uploaded the document.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AddDocumentAssetFromXDDResponse.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the document", content = @Content)
	})
	public ResponseEntity<AddDocumentAssetFromXDDResponse> createDocumentFromXDD(
			@RequestBody final AddDocumentAssetFromXDDRequest body) {

		try {
			// build initial response
			final AddDocumentAssetFromXDDResponse response = new AddDocumentAssetFromXDDResponse();
			response.setExtractionJobId(null);
			response.setPdfUploadError(false);
			response.setDocumentAssetId(null);

			// get preliminary info to build document asset
			final Document document = body.getDocument();
			final UUID projectId = body.getProjectId();
			final String doi = documentAssetService.getDocumentDoi(document);
			final UserId userId = projectService.getProject(projectId).get().getUserId();

			// get pdf url and filename
			final String fileUrl = DownloadService.getPDFURL("https://unpaywall.org/" + doi);
			final String filename = DownloadService.pdfNameFromUrl(fileUrl);

			final XDDResponse<XDDExtractionsResponseOK> extractionResponse = extractionProxy.getExtractions(doi, null, null,
					null,
					null, apikey);

			// create a new document asset from the metadata in the xdd document
			final DocumentAsset documentAsset = createDocumentAssetFromXDDDocument(document, userId,
					extractionResponse.getSuccess().getData());
			if (filename != null)
				documentAsset.getFileNames().add(filename);

			// Upload the document to TDS in order to get a new ID to pair our files we want
			// to upload with.
			final UUID newDocumentAssetId = documentAssetService.createDocumentAsset(documentAsset).getId();
			response.setDocumentAssetId(newDocumentAssetId);

			// Upload the PDF from unpaywall
			final String extractionJobId = uploadPDFFileToDocumentThenExtract(doi, filename, newDocumentAssetId);
			if (extractionJobId == null)
				response.setPdfUploadError(true);
			else
				response.setExtractionJobId(extractionJobId);

			// Now upload additional extraction files
			uploadXDDExtractions(newDocumentAssetId, extractionResponse.getSuccess().getData());

			// add asset to project
			final Optional<Project> project = projectService.getProject(projectId);
			if (project.isPresent()) {
				projectAssetService.createProjectAsset(project.get(), ResourceType.DOCUMENT,
						newDocumentAssetId);
			}

			return ResponseEntity.ok(response);

		} catch (final IOException | URISyntaxException e) {
			final String error = "Unable to upload document from github";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping(value = "/{id}/downloadDocument", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Downloads a document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Downloaded the document", content = @Content(mediaType = "application/octet-stream", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = byte[].class))),
			@ApiResponse(responseCode = "500", description = "There was an issue downloading the document", content = @Content)
	})
	public ResponseEntity<byte[]> downloadDocument(
			@PathVariable("id") final UUID id,
			@RequestParam("filename") final String filename) {

		try (final CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final PresignedURL presignedURL = documentAssetService.getDownloadUrl(id, filename);
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
				final byte[] fileAsBytes = response.getEntity().getContent().readAllBytes();
				return ResponseEntity.ok(fileAsBytes);
			}
			return ResponseEntity.status(response.getStatusLine().getStatusCode()).build();
		} catch (final Exception e) {
			final String error = "Unable to download document";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/download-document-as-text")
	@Secured(Roles.USER)
	@Operation(summary = "Downloads a document as text")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Downloaded the document", content = @Content(mediaType = "application/text", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue downloading the document", content = @Content)
	})
	public ResponseEntity<String> getDocumentFileAsText(@PathVariable("id") final UUID documentId,
			@RequestParam("filename") final String filename) {

		log.debug("Downloading document file {} for document {}", filename, documentId);

		try (final CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final PresignedURL presignedURL = documentAssetService.getDownloadUrl(documentId, filename);
			final HttpGet httpGet = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(httpGet);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return ResponseEntity.ok(textFileAsString);
		} catch (final Exception e) {

			final String error = "Unable to download document as text";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}

	}

	/**
	 * Creates a document asset from an XDD document
	 *
	 * @param document    xdd document
	 * @param username    current user name
	 * @param extractions list of extractions associated with the document
	 * @return
	 */
	private static DocumentAsset createDocumentAssetFromXDDDocument(
		final Document document,
		final UserId userId,
		final List<Extraction> extractions
	) {
		String name = document.getTitle();

		// create document asset
		final DocumentAsset documentAsset = new DocumentAsset();
		documentAsset.setName(name);
		documentAsset.setDescription(name);
		documentAsset.setUserId(userId);
		documentAsset.setFileNames(new ArrayList<>());

		if (extractions != null) {
			documentAsset.setAssets(new ArrayList<>());
			for (int i = 0; i < extractions.size(); i++) {
				final Extraction extraction = extractions.get(i);
				if (extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.FIGURE.toString())
						|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.TABLE.toString())
						|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.EQUATION.toString())) {
					final DocumentExtraction documentExtraction = new DocumentExtraction().setMetadata(new HashMap<>());
					documentExtraction.setAssetType(ExtractionAssetType.fromString(extraction.getAskemClass()));
					documentExtraction.setFileName("extraction_" + i + ".png");
					documentExtraction.getMetadata().put("title", extraction.getProperties().getTitle());
					documentExtraction.getMetadata().put("description", extraction.getProperties().getCaption());
					documentAsset.getAssets().add(documentExtraction);
					documentAsset.getFileNames().add(documentExtraction.getFileName());
				}
			}

		}

		if (document.getGithubUrls() != null && !document.getGithubUrls().isEmpty()) {
			documentAsset.setMetadata(new HashMap<>());
			documentAsset.getMetadata().put("github_urls", document.getGithubUrls());
		}

		return documentAsset;
	}

	/**
	 * Uploads the extractions associated with an XDD document
	 *
	 * @param docId
	 * @param extractions
	 */
	private void uploadXDDExtractions(final UUID docId, final List<Extraction> extractions) {

		if (extractions != null) {
			for (int i = 0; i < extractions.size(); i++) {
				final Extraction extraction = extractions.get(i);
				if (extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.FIGURE.toString())
						|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.TABLE.toString())
						|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.EQUATION.toString())) {
					final String filename = "extraction_" + i + ".png";

					try (final CloseableHttpClient httpclient = HttpClients.custom()
							.disableRedirectHandling()
							.build()) {
						final String image = extraction.getProperties().getImage();
						if (image != null) {
							final byte[] imageAsBytes = Base64.getDecoder().decode(image.getBytes(StandardCharsets.UTF_8));
							final HttpEntity fileEntity = new ByteArrayEntity(imageAsBytes,
									ContentType.APPLICATION_OCTET_STREAM);
							final PresignedURL presignedURL = documentAssetService.getUploadUrl(docId, filename);

							final HttpPut put = new HttpPut(presignedURL.getUrl());
							put.setEntity(fileEntity);

							httpclient.execute(put);
						}
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	/**
	 * Uploads a PDF file to a document asset and then fires and forgets the
	 * extraction
	 *
	 * @param doi
	 * @param filename
	 * @param docId
	 * @return
	 */
	private String uploadPDFFileToDocumentThenExtract(final String doi, final String filename, final UUID docId) {
		try (final CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final byte[] fileAsBytes = DownloadService.getPDF("https://unpaywall.org/" + doi);

			// if this service fails, return ok with errors
			if (fileAsBytes == null || fileAsBytes.length == 0) {
				return null;
			}

			// upload pdf to document asset
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			final PresignedURL presignedURL = documentAssetService.getUploadUrl(docId, filename);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse pdfUploadResponse = httpclient.execute(put);

			if (pdfUploadResponse.getStatusLine().getStatusCode() >= 400) {
				return null;
			}

			// fire and forgot pdf extractions
			return knowledgeMiddlewareProxy.postPDFToCosmos(docId).getBody().get("id").asText();

		} catch (final Exception e) {
			log.error("Unable to upload PDF document then extract", e);
			return null;
		}

	}

}
