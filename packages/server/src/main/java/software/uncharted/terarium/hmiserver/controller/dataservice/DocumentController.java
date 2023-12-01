package software.uncharted.terarium.hmiserver.controller.dataservice;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.controller.services.DocumentAssetService;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.AddDocumentAssetFromXDDRequest;
import software.uncharted.terarium.hmiserver.models.dataservice.document.AddDocumentAssetFromXDDResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/document-asset")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DocumentController implements SnakeCaseController {

	final DocumentProxy proxy;

	final ExtractionProxy extractionProxy;

	final SkemaUnifiedProxy skemaUnifiedProxy;

	final JsDelivrProxy gitHubProxy;

	final DownloadService downloadService;

	final KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

	final ProjectProxy projectProxy;

	final DocumentAssetService documentAssetService;

	final ObjectMapper objectMapper;

	@Value("${xdd.api-key}")
	String apikey;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<DocumentAsset>> getDocuments(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {
		return ResponseEntity.ok(documentAssetService.getDocumentAssets(page, pageSize));
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createDocument(
		@RequestBody final DocumentAsset document
	) {
		documentAssetService.createDocumentAsset(document);

		JsonNode res = objectMapper.valueToTree(Map.of("id", document.getId()));

		return ResponseEntity.ok(res);
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<DocumentAsset> getDocument(
		@PathVariable("id") final String id
	) {
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
				final String url = proxy.getDownloadUrl(id, asset.getFileName()).getBody().getUrl();
				asset.getMetadata().put("url", url);

				// if the asset os of type equation
				if (asset.getAssetType().equals(ExtractionAssetType.EQUATION) && asset.getMetadata().get("equation") == null) {
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
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> deleteDocument(
		@PathVariable("id") final String id
	) {
		documentAssetService.deleteDocumentAsset(id);

		JsonNode res = objectMapper.valueToTree(Map.of("message", String.format("Document successfully deleted: %s", id)));

		return ResponseEntity.ok(res);
	}

	/**
	 * Uploads an artifact inside the entity to TDS via a presigned URL
	 *
	 * @param documentId         The ID of the document to upload to
	 * @param fileName           The name of the file to upload
	 * @param fileEntity  		 The entity containing the file to upload
	 * @return A response containing the status of the upload
	 */
	private ResponseEntity<Integer> uploadDocumentHelper(final String documentId, final String fileName, final HttpEntity fileEntity) {
		try (final CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = proxy.getUploadUrl(documentId, fileName).getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpclient.execute(put);

			// if the fileEntity is not a PDF, then we need to extract the text and update the document asset
			if (!DownloadService.IsPdf(fileEntity.getContent().readAllBytes())) {
				final DocumentAsset document = documentAssetService.getDocumentAsset(documentId);

				document.setText(IOUtils.toString(fileEntity.getContent(), StandardCharsets.UTF_8));

				documentAssetService.updateDocumentAsset(document);
			}

			return ResponseEntity.ok(response.getStatusLine().getStatusCode());

		} catch (final Exception e) {
			log.error("Unable to PUT artifact data", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * Uploads a file to the project.
	 */
	@PutMapping(value = "/{id}/uploadDocument", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<Integer> uploadDocument(
		@PathVariable("id") final String id,
		@RequestParam("filename") final String filename,
		@RequestPart("file") final MultipartFile file
	) throws IOException {
		final byte[] fileAsBytes = file.getBytes();
		final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		return uploadDocumentHelper(id, filename, fileEntity);
	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it to the project.
	 */
	@PutMapping("/{documentId}/uploadDocumentFromGithub")
	@Secured(Roles.USER)
	public ResponseEntity<Integer> uploadDocumentFromGithub(
		@PathVariable("documentId") final String documentId,
		@RequestParam("path") final String path,
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("filename") final String filename
	) {
		log.debug("Uploading Document file from github to dataset {}", documentId);

		//download file from GitHub
		final String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path).getBody();
		final HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadDocumentHelper(documentId, filename, fileEntity);

	}

	@PostMapping(value = "/createDocumentFromXDD")
	@Secured(Roles.USER)
	public ResponseEntity<AddDocumentAssetFromXDDResponse> createDocumentFromXDD(
		@RequestBody final AddDocumentAssetFromXDDRequest body
	) {


		//build initial response
		AddDocumentAssetFromXDDResponse response = new AddDocumentAssetFromXDDResponse();
		response.setExtractionJobId(null);
		response.setPdfUploadError(false);
		response.setDocumentAssetId(null);


		// get preliminary info to build document asset
		Document document = body.getDocument();
		String projectId = body.getProjectId();
		String doi = documentAssetService.getDocumentDoi(document);
		String username = "";//TODO dvince:projectProxy.getProject(projectId).getBody().getUsername();

		// get pdf url and filename
		String fileUrl = null;
		String filename = null;
		try {
			fileUrl = DownloadService.getPDFURL("https://unpaywall.org/" + doi);
			filename = DownloadService.pdfNameFromUrl(fileUrl);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		XDDResponse<XDDExtractionsResponseOK> extractionResponse = extractionProxy.getExtractions(doi, null, null, null, null, apikey);

		// create a new document asset from the metadata in the xdd document
		DocumentAsset documentAsset = createDocumentAssetFromXDDDocument(document, username, extractionResponse.getSuccess().getData());
		if(filename != null)
			documentAsset.getFileNames().add(filename);

		// Upload the document to TDS in order to get a new ID to pair our files we want to upload with.
		final String newDocumentAssetId = documentAssetService.createDocumentAsset(documentAsset).getId();
		response.setDocumentAssetId(newDocumentAssetId);

		// Upload the PDF from unpaywall
		String extractionJobId = uploadPDFFileToDocumentThenExtract(doi, filename, newDocumentAssetId);
		if(extractionJobId == null)
			response.setPdfUploadError(true);
		else
			response.setExtractionJobId(extractionJobId);

		// Now upload additional extraction files
		uploadXDDExtractions(newDocumentAssetId, extractionResponse.getSuccess().getData());

		//add asset to project
		projectProxy.createAsset(projectId, AssetType.documents, newDocumentAssetId);

		return ResponseEntity.ok(response);

	}


	@GetMapping(value = "/{id}/downloadDocument", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Secured(Roles.USER)
	public ResponseEntity<byte[]> downloadDocument(
		@PathVariable("id") final String id,
		@RequestParam("filename") final String filename
	) throws IOException {
		try (final CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			final PresignedURL presignedURL = proxy.getDownloadUrl(id, filename).getBody();
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
				final byte[] fileAsBytes = response.getEntity().getContent().readAllBytes();
				return ResponseEntity.ok(fileAsBytes);
			}
			return ResponseEntity.status(response.getStatusLine().getStatusCode()).build();
		} catch (final Exception e) {
			log.error("Unable to GET document data", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/{id}/download-document-as-text")
	@Secured(Roles.USER)
	public ResponseEntity<String> getDocumentFileAsText(@PathVariable("id") final String documentId, @RequestParam("filename") final String filename) {

		log.debug("Downloading document file {} for document {}", filename, documentId);

		try (final CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			final PresignedURL presignedURL = proxy.getDownloadUrl(documentId, filename).getBody();
			final HttpGet httpGet = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(httpGet);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return ResponseEntity.ok(textFileAsString);
		} catch (final Exception e) {
			log.error("Unable to GET document data", e);
			return ResponseEntity.internalServerError().build();
		}

	}

	/**
	 * Creates a document asset from an XDD document
	 * @param document xdd document
	 * @param username current user name
	 * @param extractions list of extractions associated with the document
	 * @return
	 */
	private DocumentAsset createDocumentAssetFromXDDDocument(Document document, String username, List<Extraction> extractions) {
		String name = document.getTitle();

		//create document asset
		DocumentAsset documentAsset = new DocumentAsset();
		documentAsset.setName(name);
		documentAsset.setDescription(name);
		documentAsset.setUsername(username);
		documentAsset.setFileNames(new ArrayList<>());

		if(extractions != null) {
			documentAsset.setAssets(new ArrayList<>());
			for(int i = 0; i < extractions.size(); i++) {
				Extraction extraction = extractions.get(i);
				if(extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.FIGURE.toString())
				|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.TABLE.toString())
				|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.EQUATION.toString()) ) {
					DocumentExtraction documentExtraction = new DocumentExtraction().setMetadata(new HashMap<>());
					documentExtraction.setAssetType(ExtractionAssetType.fromString(extraction.getAskemClass()));
					documentExtraction.setFileName("extraction_" + i + ".png");
					documentExtraction.getMetadata().put("title", extraction.getProperties().getTitle());
					documentExtraction.getMetadata().put("description", extraction.getProperties().getCaption());
					documentAsset.getAssets().add(documentExtraction);
					documentAsset.getFileNames().add(documentExtraction.getFileName());
				}
			}

		}

		if(document.getGithubUrls() != null && !document.getGithubUrls().isEmpty()){
			documentAsset.setMetadata(new HashMap<>());
			documentAsset.getMetadata().put("github_urls", document.getGithubUrls());
		}

		return documentAsset;
	}

	/**
	 * Uploads the extractions associated with an XDD document
	 * @param docId
	 * @param extractions
	 */
	private void uploadXDDExtractions(String docId, List<Extraction> extractions){

		if(extractions != null) {
			for (int i = 0; i < extractions.size(); i++) {
				Extraction extraction = extractions.get(i);
				if (extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.FIGURE.toString())
				|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.TABLE.toString())
				|| extraction.getAskemClass().equalsIgnoreCase(ExtractionAssetType.EQUATION.toString())) {
					String filename = "extraction_" + i + ".png";

					try(CloseableHttpClient httpclient = HttpClients.custom()
						.disableRedirectHandling()
						.build()){
						String image = extraction.getProperties().getImage();
						if(image != null){
							byte[] imageAsBytes = Base64.getDecoder().decode(image.getBytes("UTF-8"));
							HttpEntity fileEntity = new ByteArrayEntity(imageAsBytes, ContentType.APPLICATION_OCTET_STREAM);
							final PresignedURL presignedURL = proxy.getUploadUrl(docId, filename).getBody();

							final HttpPut put = new HttpPut(presignedURL.getUrl());
							put.setEntity(fileEntity);

							httpclient.execute(put);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	/**
	 * Uploads a PDF file to a document asset and then fires and forgets the extraction
	 * @param doi
	 * @param filename
	 * @param docId
	 * @return
	 */
	private String uploadPDFFileToDocumentThenExtract(String doi, String filename, String docId){
		try(CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()){

			byte[] fileAsBytes = downloadService.getPDF("https://unpaywall.org/" + doi);

			//if this service fails, return ok with errors
			if(fileAsBytes == null || fileAsBytes.length == 0){
				return null;
			}

			// upload pdf to document asset
			HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			final PresignedURL presignedURL = proxy.getUploadUrl(docId, filename).getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse pdfUploadResponse = httpclient.execute(put);

			if(pdfUploadResponse.getStatusLine().getStatusCode() >= 400) {
				return null;
			}

			// fire and forgot pdf extractions
			return knowledgeMiddlewareProxy.postPDFToCosmos(docId).getBody().get("id").asText();

		} catch (Exception e){
			log.error("Unable to upload PDF document then extract", e);
			return null;
		}

	}


}
