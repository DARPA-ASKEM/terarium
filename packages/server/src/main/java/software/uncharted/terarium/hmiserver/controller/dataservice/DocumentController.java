package software.uncharted.terarium.hmiserver.controller.dataservice;


import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.controller.services.DocumentAssetService;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.AddDocumentAssetFromXDDRequest;
import software.uncharted.terarium.hmiserver.models.dataservice.document.AddDocumentAssetFromXDDResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.proxies.knowledge.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RequestMapping("/document-asset")
@RestController
@Slf4j
public class DocumentController implements SnakeCaseController {

	@Autowired
	DocumentProxy proxy;

	@Autowired
	SkemaUnifiedProxy skemaUnifiedProxy;

	@Autowired
	JsDelivrProxy gitHubProxy;

	@Autowired
	KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

	@Autowired
	ProjectProxy projectProxy;

	@Autowired
	DocumentAssetService documentAssetService;

	@GetMapping
	public ResponseEntity<List<DocumentAsset>> getDocuments(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {

		return ResponseEntity.ok(proxy.getAssets(pageSize, page).getBody());
	}

	@PostMapping
	public ResponseEntity<JsonNode> createDocument(
		@RequestBody final DocumentAsset document
	) {
		return ResponseEntity.ok(proxy.createAsset(convertObjectToSnakeCaseJsonNode(document)).getBody());
	}

	@GetMapping("/{id}")
	public ResponseEntity<DocumentAsset> getDocument(
		@PathVariable("id") final String id
	) {
		final DocumentAsset document = proxy.getAsset(id).getBody();

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
				if (asset.getAssetType().equals("equation") && asset.getMetadata().get("equation") == null) {
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
		proxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(document));

		// Return the updated document
		return ResponseEntity.ok(document);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<JsonNode> deleteDocument(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(proxy.deleteAsset(id).getBody());
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
				final JsonNode node = this.convertObjectToSnakeCaseJsonNode(proxy.getAsset(documentId).getBody().setText(IOUtils.toString(fileEntity.getContent(), StandardCharsets.UTF_8)));
				final ResponseEntity<JsonNode> updateRes = proxy.updateAsset(documentId, node);
				if (updateRes.getStatusCode().isError()) {
					return ResponseEntity.status(updateRes.getStatusCode()).build();
				}
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
	public ResponseEntity<AddDocumentAssetFromXDDResponse> createDocumentFromXDD(
		@RequestBody final AddDocumentAssetFromXDDRequest body
	) {
		try (final CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			//build initial response
			final AddDocumentAssetFromXDDResponse response = new AddDocumentAssetFromXDDResponse();
			response.setExtractionJobId(null);
			response.setPdfUploadError(false);
			response.setDocumentAssetId(null);


			// get preliminary info to build document asset
			final Document document = body.getDocument();
			final String projectId = body.getProjectId();
			final String name = document.getTitle();
			final String username = projectProxy.getProject(projectId).getBody().getUsername();
			final String doi = documentAssetService.getDocumentDoi(document);
			final String fileUrl = DownloadService.getPDFURL("https://unpaywall.org/" + doi);
			final String filename = DownloadService.pdfNameFromUrl(fileUrl);
			final List<String> filenames = new ArrayList<String>();
			if(filename != null){
				filenames.add(filename);
			}

			//create document asset
			final DocumentAsset documentAsset = new DocumentAsset();
			documentAsset.setName(name);
			documentAsset.setDescription(name);
			documentAsset.setUsername(username);
			documentAsset.setFileNames(filenames);

			final String newDocumentAssetId = proxy.createAsset(convertObjectToSnakeCaseJsonNode(documentAsset)).getBody().get("id").asText();
			response.setDocumentAssetId(newDocumentAssetId);

			//add asset to project
			projectProxy.createAsset(projectId, AssetType.documents, newDocumentAssetId);

			//if there is no filename that means we cannot get the pdf return ok with errors.
			if(filename == null || filename.isEmpty()){
				response.setPdfUploadError(true);
				return ResponseEntity.ok(response);
			}

			final byte[] fileAsBytes = DownloadService.getPDF("https://unpaywall.org/" + doi);

			//if this service fails, return ok with errors
			if(fileAsBytes == null || fileAsBytes.length == 0){
				response.setPdfUploadError(true);
				return ResponseEntity.ok(response);
			}

			// upload pdf to document asset
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			final PresignedURL presignedURL = proxy.getUploadUrl(newDocumentAssetId, filename).getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse pdfUploadResponse = httpclient.execute(put);

			if(pdfUploadResponse.getStatusLine().getStatusCode() >= 400) {
				response.setPdfUploadError(true);
				return ResponseEntity.ok(response);
			}

			// fire and forgot pdf extractions
			final String jobId = knowledgeMiddlewareProxy.postPDFToCosmos(newDocumentAssetId).getBody().get("id").asText();
			response.setExtractionJobId(jobId);

			return ResponseEntity.ok(response);
		} catch (final Exception e) {
			log.error("Unable to GET document data", e);
			return ResponseEntity.internalServerError().build();
		}
	}


	@GetMapping(value = "/{id}/downloadDocument", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
}
