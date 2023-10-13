package software.uncharted.terarium.hmiserver.controller.dataservice;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import org.apache.http.entity.StringEntity;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RequestMapping("/document-asset")
@RestController
@Slf4j
public class DocumentController implements SnakeCaseController {

	private final DownloadService downloadService;
	@Autowired
	DocumentProxy proxy;

	@Autowired
	JsDelivrProxy gitHubProxy;


    @Autowired
    public DocumentController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

	@GetMapping
	public ResponseEntity<List<DocumentAsset>> getDocuments(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {

		return ResponseEntity.ok(proxy.getAssets(pageSize, page).getBody());
	}

	@PostMapping
	public ResponseEntity<JsonNode> createDocument(
		@RequestBody DocumentAsset document
	) {
		return ResponseEntity.ok(proxy.createAsset(convertObjectToSnakeCaseJsonNode(document)).getBody());
	}

	@GetMapping("/{id}")
	public ResponseEntity<DocumentAsset> getDocument(
		@PathVariable("id") String id
	) {
		return ResponseEntity.ok(proxy.getAsset(id).getBody());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<JsonNode> deleteDocument(
		@PathVariable("id") String id
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
	private ResponseEntity<Integer> uploadDocumentHelper(String documentId, String fileName, HttpEntity fileEntity) {
		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = proxy.getUploadUrl(documentId, fileName).getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpclient.execute(put);

			return ResponseEntity.ok(response.getStatusLine().getStatusCode());

		} catch (Exception e) {
			log.error("Unable to PUT artifact data", e);
			return ResponseEntity.internalServerError().build();
		}
	}



	@PutMapping(value = "/{id}/uploadDocument", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Integer> uploadDocument(
		@PathVariable("id") String id,
		@RequestParam("filename") final String filename,
		@RequestPart("file") MultipartFile file
	) throws IOException {
		byte[] fileAsBytes = file.getBytes();
		HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
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
		String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path).getBody();
		HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadDocumentHelper(documentId, filename, fileEntity);

	}

	@PutMapping(value = "/{id}/uploadDocumentFromDOI")
	public ResponseEntity<Integer> uploadDocumentFromDOI(
		@PathVariable("id") String id,
		@RequestParam("doi") String doi
	) {
		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {
			byte[] fileAsBytes = downloadService.getPDF("https://unpaywall.org/" + doi);

			HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);

			final PresignedURL presignedURL = proxy.getUploadUrl(id, "paper.pdf").getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpclient.execute(put);


			return ResponseEntity.ok(response.getStatusLine().getStatusCode());
			
		} catch (Exception e) {
			log.error("Unable to PUT document data", e);
			return ResponseEntity.internalServerError().build();
		}
		
	}


	@GetMapping(value = "/{id}/downloadDocument", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> downloadDocument(
		@PathVariable("id") String id,
		@RequestParam("filename") final String filename
	) throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			final PresignedURL presignedURL = proxy.getDownloadUrl(id, filename).getBody();
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
				byte[] fileAsBytes = response.getEntity().getContent().readAllBytes();
				return ResponseEntity.ok(fileAsBytes);
			}
			return ResponseEntity.status(response.getStatusLine().getStatusCode()).build();
		} catch (Exception e) {
			log.error("Unable to GET document data", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/{id}/download-document-as-text")
	public ResponseEntity<String> getDocumentFileAsText(@PathVariable("id") String documentId, @RequestParam("filename") String filename) {

		log.debug("Downloading document file {} for document {}", filename, documentId);

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			PresignedURL presignedURL = proxy.getDownloadUrl(documentId, filename).getBody();
			final HttpGet httpGet = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(httpGet);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return ResponseEntity.ok(textFileAsString);


		} catch (Exception e) {
			log.error("Unable to GET document data", e);
			return ResponseEntity.internalServerError().build();
		}

	}


}
