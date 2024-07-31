package software.uncharted.terarium.hmiserver.proxies.skema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

@FeignClient(name = "skema-unified", url = "${skema-unified.url}")
public interface SkemaUnifiedProxy {
	@PostMapping("/workflows/latex/equations-to-amr")
	ResponseEntity<Model> postLaTeXToAMR(@RequestBody JsonNode request);

	@PostMapping("/eqn2mml/image/base64/mml")
	ResponseEntity<String> postImageToEquations(@RequestBody String request);

	@PostMapping("/workflows/consolidated/equations-to-amr")
	ResponseEntity<Model> consolidatedEquationsToAMR(@RequestBody JsonNode request);

	@PostMapping("/workflows/images/base64/equations-to-amr")
	ResponseEntity<Model> base64EquationsToAMR(@RequestBody JsonNode request);

	@PostMapping("/workflows/images/base64/equations-to-latex")
	ResponseEntity<String> base64EquationsToLatex(@RequestBody JsonNode request);

	@PostMapping(value = "/workflows/code/llm-assisted-codebase-to-pn-amr", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> llmCodebaseToAMR(@RequestPart("zip_file") MultipartFile file);

	@PostMapping(value = "/workflows/code/codebase-to-pn-amr", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> codebaseToAMR(@RequestPart("zip_file") MultipartFile file);

	@PostMapping(value = "/workflows/code/snippets-to-amr", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> snippetsToAMR(
		@RequestPart("files") List<String> files,
		@RequestPart("blobs") List<String> blobs
	);

	@PostMapping(value = "/metal/link_amr", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> linkAMRFile(
		@RequestPart("amr_file") MultipartFile amrFile,
		@RequestPart("text_extractions_file") MultipartFile extractionsFile
	);

	@Data
	class IntegratedTextExtractionsBody {

		public IntegratedTextExtractionsBody(final String text) {
			this.texts = Arrays.asList(text);
			this.amrs = new ArrayList<>();
		}

		public IntegratedTextExtractionsBody(final String text, final List<Model> amrs) {
			this.texts = Arrays.asList(text);
			this.amrs = amrs
				.stream()
				.map(amr -> {
					try {
						final ObjectMapper mapper = new ObjectMapper();
						return mapper.writeValueAsString(amr);
					} catch (final JsonProcessingException e) {
						e.printStackTrace();
						return null;
					}
				})
				.collect(Collectors.toList());
		}

		final List<String> texts;
		final List<String> amrs;
	}

	@PostMapping("/text-reading/integrated-text-extractions")
	ResponseEntity<JsonNode> integratedTextExtractions(
		@RequestParam(value = "annotate_mit", defaultValue = "true") Boolean annotateMit,
		@RequestParam(value = "annotate_skema", defaultValue = "false") Boolean annotateSkema,
		@RequestBody IntegratedTextExtractionsBody body
	);
}
