package software.uncharted.terarium.hmiserver.proxies.documentservice;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "extractionService", url = "${xdd-prod-service.url}")
public interface ExtractionProxy {
	@PostMapping(value = "/cosmos_service/process/", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> processPdfExtraction(
		@RequestPart("compress_images") final Boolean compressImages,
		@RequestPart("use_cache") final Boolean useCache,
		@RequestPart("pdf") MultipartFile file
	);

	@GetMapping(value = "/cosmos_service/process/{id}/result")
	ResponseEntity<byte[]> result(@PathVariable("id") final UUID jobId);

	@GetMapping(value = "/cosmos_service/process/{id}/result/text")
	ResponseEntity<JsonNode> text(@PathVariable("id") final UUID jobId);

	@GetMapping(value = "/cosmos_service/process/{id}/result/extractions/{extraction-type}")
	ResponseEntity<JsonNode> extraction(
		@PathVariable("id") final UUID jobId,
		@PathVariable("extraction-type") final String type
	);

	@GetMapping(value = "/cosmos_service/process/{id}/status")
	ResponseEntity<JsonNode> status(@PathVariable("id") final UUID jobId);
}
