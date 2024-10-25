package software.uncharted.terarium.hmiserver.proxies.mit;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "mit", url = "${mit-proxy.url}")
public interface MitProxy {
	@PostMapping(value = "/cards/get_data_card", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> dataCard(
		@RequestParam("gpt_key") final String gptKey,
		@RequestPart("csv_file") MultipartFile textFile,
		@RequestPart("doc_file") MultipartFile codeFile
	);
}
