package software.uncharted.terarium.hmiserver.controller.documentservice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
@RequiredArgsConstructor
public class ExtractionController {


	// source: https://www.crossref.org/blog/dois-and-matching-regular-expressions/
	private static final Pattern DOI_VALIDATION_PATTERN = Pattern.compile("^10.\\d{4,9}\\/[-._;()\\/:A-Z0-9]+$", Pattern.CASE_INSENSITIVE);


	@Value("${xdd.api-key}")
	String key;

	@Value("${xdd.api-es-key}")
	String ESkey;
	final ExtractionProxy proxy;


	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<XDDResponse<XDDExtractionsResponseOK>> searchExtractions(
		@RequestParam(required = false, name = "term") final String term,
		@RequestParam(required = false, name = "page") final Integer page,
		@RequestParam(required = false, name = "ASKEM_CLASS") final String askemClass,
		@RequestParam(required = false, name = "include_highlights") final String include_highlights) {


		final Matcher matcher = DOI_VALIDATION_PATTERN.matcher(term);

		final boolean isDoi = matcher.find();
		String apiKey = "";
		if (key != null)
			apiKey = key;
		else
			log.info("XDD API key missing. Image assets will not return correctly.");

		try {
			final XDDResponse<XDDExtractionsResponseOK> response;
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


		} catch (final FeignException e) {
			log.error("xDD returned an exception for extraction search:", e);
			throw new ResponseStatusException(HttpStatusCode.valueOf(e.status()), "There was an issue with the extraction search to xDD");
		} catch (final Exception e) {
			log.error("Unable to find extractions, an error occurred", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find extractions, an error occurred");
		}


	}

	@GetMapping("/askem-autocomplete/{term}")
	@Secured(Roles.USER)
	public ResponseEntity<List<String>> getAutocomplete(@PathVariable("term") final String term) {
		try {

			final AutoComplete autoComplete = proxy.getAutocomplete(term);
			if (autoComplete.hasNoSuggestions())
				return ResponseEntity.noContent().build();

			return ResponseEntity.ok(autoComplete.getAutoCompletes());


		} catch (final FeignException e) {
			log.error("xDD returned an exception for autocomplete:", e);
			throw new ResponseStatusException(HttpStatusCode.valueOf(e.status()), "xDD returned an exception for autocomplete");
		} catch (final Exception e) {
			log.error("Unable to find xdd Autocompletes", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find xdd Autocompletes");
		}
	}
}
