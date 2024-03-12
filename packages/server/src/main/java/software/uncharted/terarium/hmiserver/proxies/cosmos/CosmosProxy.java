package software.uncharted.terarium.hmiserver.proxies.cosmos;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(name = "cosmos", url = "${cosmos-proxy.url}")
public interface CosmosProxy {

	@PostMapping(value = "/process", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> processPdfExtraction(
			@RequestParam("compress_images") final Boolean compressImages,
			@RequestParam("use_cache") final Boolean useCache,
			@RequestPart(value = "pdf") MultipartFile file);
}
