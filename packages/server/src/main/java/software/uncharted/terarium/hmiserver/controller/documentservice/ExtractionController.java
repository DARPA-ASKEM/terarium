package software.uncharted.terarium.hmiserver.controller.documentservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.documentservice.autocomplete.AutoComplete;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/document/extractions")
@RestController
@Slf4j
public class ExtractionController {


	// source: https://www.crossref.org/blog/dois-and-matching-regular-expressions/
	private static final Pattern DOI_VALIDATION_PATTERN = Pattern.compile("^10.\\d{4,9}\\/[-._;()\\/:A-Z0-9]+$", Pattern.CASE_INSENSITIVE);


	@Value("${xdd.api-key}")
	String key;

	@Value("${xdd.api-es-key}")
	String ESkey;

	@Autowired
	ExtractionProxy proxy;


	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<XDDResponse<XDDExtractionsResponseOK>> searchExtractions(
		@RequestParam(required = false, name = "term") final String term,
		@RequestParam(required = false, name = "page") final Integer page,
		@RequestParam(required = false, name = "ASKEM_CLASS") String askemClass,
		@RequestParam(required = false, name = "include_highlights") String include_highlights) {


		Matcher matcher = DOI_VALIDATION_PATTERN.matcher(term);

		boolean isDoi = matcher.find();
		String apiKey = "";
		if (key != null)
			apiKey = key;
		else
			log.info("XDD API key missing. Image assets will not return correctly.");

		try {
			XDDResponse<XDDExtractionsResponseOK> response;
			if (isDoi) {
				response = proxy.getExtractions(term, null, page, askemClass, include_highlights, apiKey);
			} else {
				response = proxy.getExtractions(null, term, page, askemClass, include_highlights, apiKey);
			}

			if (response.getErrorMessage() != null) {
				return ResponseEntity.internalServerError().build();
			}

			if (response.getSuccess() == null || response.getSuccess().getData().isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			return ResponseEntity.ok(response);


		} catch (RuntimeException e) {
			log.error("Unable to search in extractions. An error occurred", e);
			return ResponseEntity.internalServerError().build();

		}


	}

	@GetMapping("/askem-autocomplete/{term}")
	@Secured(Roles.USER)
	public ResponseEntity<List<String>> getAutocomplete(@PathVariable("term") String term) {
		try {

			AutoComplete autoComplete = proxy.getAutocomplete(term);
			if (autoComplete.hasNoSuggestions())
				return ResponseEntity.noContent().build();

			return ResponseEntity.ok(autoComplete.getAutoCompletes());


		} catch (RuntimeException e) {
			log.error("Unable to autocomplete");
			return ResponseEntity.internalServerError().build();
		}
	}
}
