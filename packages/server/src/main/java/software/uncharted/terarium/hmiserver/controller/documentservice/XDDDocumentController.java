package software.uncharted.terarium.hmiserver.controller.documentservice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.DocumentsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/documents")
@RestController
@Slf4j
@RequiredArgsConstructor
public class XDDDocumentController {

	final DocumentProxy proxy;

	@Value("${xdd.api-es-key}")
	String api_es_key;

	// NOTE: the query parameters match the proxy version and the type XDDSearchPayload
	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<XDDResponse<DocumentsResponseOK>> getDocuments(
			@RequestParam(required = false, name = "docid") final String docid,
			@RequestParam(required = false, name = "doi") final String doi,
			@RequestParam(required = false, name = "title") String title,
			@RequestParam(required = false, name = "term") String term,
			@RequestParam(required = false, name = "dataset") String dataset,
			@RequestParam(required = false, name = "include_score") String include_score,
			@RequestParam(required = false, name = "include_highlights") String include_highlights,
			@RequestParam(required = false, name = "inclusive") String inclusive,
			@RequestParam(required = false, name = "full_results") String full_results,
			@RequestParam(required = false, name = "max") String max,
			@RequestParam(required = false, name = "per_page") String per_page,
			@RequestParam(required = false, name = "dict") String dict,
			@RequestParam(required = false, name = "facets") final String facets,
			@RequestParam(required = false, name = "min_published") String min_published,
			@RequestParam(required = false, name = "max_published") String max_published,
			@RequestParam(required = false, name = "pubname") String pubname,
			@RequestParam(required = false, name = "publisher") String publisher,
			@RequestParam(required = false, name = "additional_fields") String additional_fields,
			@RequestParam(required = false, name = "match") String match,
			@RequestParam(required = false, name = "known_entities") final String known_entities,
			@RequestParam(required = false, name = "github_url") final String github_url,
			@RequestParam(required = false, name = "similar_to") final String similar_to,
			@RequestParam(required = false, name = "askem_object_limit", defaultValue = "5")
					final String askem_object_limit) {

		// only go ahead with the query if at least one param is present
		if (docid != null || doi != null || term != null || github_url != null || similar_to != null) {
			// for a more direct search, if doi is valid, then make sure other params are null
			if (docid != null || doi != null) {
				title = null;
				term = null;
				dataset = null;
				include_score = null;
				include_highlights = null;
				inclusive = null;
				full_results = null;
				max = null;
				per_page = null;
				dict = null;
				min_published = null;
				max_published = null;
				pubname = null;
				publisher = null;
				additional_fields = null;
				match = null;
			}

			try {
				String apiKey = "";
				if (api_es_key != null) apiKey = api_es_key;
				else {
					log.error("XDD API key missing. Extractions will not work");
					return ResponseEntity.internalServerError().build();
				}

				final XDDResponse<DocumentsResponseOK> doc = proxy.getDocuments(
						apiKey,
						docid,
						doi,
						title,
						term,
						dataset,
						include_score,
						include_highlights,
						inclusive,
						full_results,
						max,
						per_page,
						dict,
						facets,
						min_published,
						max_published,
						pubname,
						publisher,
						additional_fields,
						match,
						known_entities,
						github_url,
						similar_to,
						askem_object_limit);

				if (doc.getErrorMessage() != null) {
					return ResponseEntity.internalServerError().build();
				}

				if (doc.getSuccess() == null || doc.getSuccess().getData().isEmpty()) {
					return ResponseEntity.noContent().build();
				}

				return ResponseEntity.ok().body(doc);

			} catch (final FeignException e) {
				log.error("xDD returned an exception for document search:", e);
				throw new ResponseStatusException(
						HttpStatusCode.valueOf(e.status()), "There was an issue with the request to xDD");
			} catch (final Exception e) {
				log.error("Unable to find documents, an error occurred", e);
				throw new ResponseStatusException(
						HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find documents, an error occurred");
			}
		}
		throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"At least one parameter must be present: docid, doi, term, github_url, similar_to");
	}
}
