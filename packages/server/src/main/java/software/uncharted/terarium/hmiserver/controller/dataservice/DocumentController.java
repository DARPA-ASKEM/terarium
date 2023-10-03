package software.uncharted.terarium.hmiserver.controller.dataservice;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DocumentProxy;

import java.io.IOException;
import java.util.List;

@RequestMapping("/document-asset")
@RestController
@Slf4j
public class DocumentController implements SnakeCaseController {

	@Autowired
	DocumentProxy proxy;

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

	@PutMapping(value = "/{id}/uploadDocument", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Integer> uploadDocument(
		@PathVariable("id") String id,
		@RequestParam("filename") final String filename,
		@RequestPart("file") MultipartFile file
	) throws IOException {
		byte[] fileAsBytes = file.getBytes();
		HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = proxy.getUploadUrl(id, filename).getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpclient.execute(put);

			return ResponseEntity.ok(response.getStatusLine().getStatusCode());


		} catch (Exception e) {
			log.error("Unable to PUT artifact data", e);
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
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(put);
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
}
