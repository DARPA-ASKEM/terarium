package software.uncharted.terarium.hmiserver.proxies.mit;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(name = "mit", url = "${mit-proxy.url}")
public interface MitProxy {

	@PostMapping("/integration/get_mapping")
	ResponseEntity<JsonNode> getMapping(
			@RequestParam("gpt_key") final String gptKey,
			@RequestParam("kg_domain") final String kgDomain,
			@RequestPart(value = "mit_file") MultipartFile mitFile,
			@RequestPart(value = "arizona_file") MultipartFile arizonaFile);

	@PostMapping("/annotation/upload_file_extract")
	ResponseEntity<JsonNode> uploadFileExtract(
			@RequestParam("gpt_key") final String gptKey,
			@RequestPart(value = "file") MultipartFile file);
}
