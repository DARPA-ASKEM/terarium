package software.uncharted.terarium.hmiserver.proxies.documentservice;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import software.uncharted.terarium.hmiserver.models.documentservice.autocomplete.AutoComplete;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;

@FeignClient(name = "extractionService", url = "${xdd-dev-service.url}")
public interface ExtractionProxy {
	@GetMapping("/askem/object")
	XDDResponse<XDDExtractionsResponseOK> getExtractions(
		@RequestParam(required = false, name = "doi") final String doi,
		@RequestParam(required = false, name = "query_all") final String queryAll,
		@RequestParam(required = false, name = "page") final Integer page,
		@RequestParam(required = false, name = "ASKEM_CLASS") String askemClass,
		@RequestParam(required = false, name = "include_highlights") String include_highlights,
		@RequestParam(required = false, name = "api_key") String apiKey
	);

	@GetMapping("askem_autocomplete/{term}")
	AutoComplete getAutocomplete(
		@PathVariable("term") final String term
	);
}
