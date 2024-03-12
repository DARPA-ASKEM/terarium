package software.uncharted.terarium.hmiserver.proxies.skema;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
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

	@PostMapping(value = "/metal/link_amr", consumes = "multipart/form-data")
	ResponseEntity<JsonNode> linkAMRFile(
			@RequestPart(value = "amr_file") MultipartFile amrFile,
			@RequestPart(value = "text_extractions_file") MultipartFile extractionsFile);

	@Data
	public static class IntegratedTextExtractionsBody {

		public IntegratedTextExtractionsBody(final String text) {
			this.texts = Arrays.asList(text);
		}

		final List<String> texts;
	}

	@PostMapping("/text-reading/integrated-text-extractions")
	ResponseEntity<JsonNode> integratedTextExtractions(
			@RequestParam(value = "annotate_mit", defaultValue = "true") Boolean annotateMit,
			@RequestParam(value = "annotate_skema", defaultValue = "true") Boolean annotateSkema,
			@RequestBody IntegratedTextExtractionsBody body);

}
