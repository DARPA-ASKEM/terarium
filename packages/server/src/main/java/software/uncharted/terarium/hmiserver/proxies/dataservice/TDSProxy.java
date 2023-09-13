package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;

import java.util.List;

@FeignClient(name = "tds", url = "${terarium.dataservice.url}")
public interface TDSProxy<T> {

	@GetMapping
	ResponseEntity<List<T>> getAssets(
		@RequestParam(name = "page_size", defaultValue="500") final Integer pageSize,
		@RequestParam(name = "page", defaultValue="0") final Integer page
	);

	@PostMapping
	ResponseEntity<JsonNode> createAsset(
		@RequestBody JsonNode asset
	);

	@GetMapping("/{id}")
	ResponseEntity<T> getAsset(
		@PathVariable("id") String id
	);

	@DeleteMapping("/{id}")
	ResponseEntity<JsonNode> deleteAsset(
		@PathVariable("id") String id
	);

	@PatchMapping("/{id}")
	ResponseEntity<JsonNode> updateAsset(
		@PathVariable("id") String id,
		@RequestBody JsonNode asset
	);

	@PostMapping("/deprecate/{id}")
	ResponseEntity<JsonNode>  deprecateAsset(
		@PathVariable("id") String id
	);


	@GetMapping("/{id}/upload-url")
	ResponseEntity<PresignedURL>  getUploadUrl(
		@PathVariable("id") String id,
		@RequestParam("filename") String filename
	);

	@GetMapping("/{id}/download-url")
	ResponseEntity<PresignedURL> getDownloadUrl(
		@PathVariable("id") String id, @RequestParam("filename") String filename
	);

}
