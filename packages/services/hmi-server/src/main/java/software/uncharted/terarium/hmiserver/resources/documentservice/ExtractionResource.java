package software.uncharted.terarium.hmiserver.resources.documentservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.documentservice.autocomplete.AutoComplete;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Document Extraction REST Endpoint")
@Path("/api/document/extractions")
@Slf4j
public class ExtractionResource {


	// source: https://www.crossref.org/blog/dois-and-matching-regular-expressions/
	private static final Pattern DOI_VALIDATION_PATTERN = Pattern.compile("^10.\\d{4,9}\\/[-._;()\\/:A-Z0-9]+$", Pattern.CASE_INSENSITIVE);

	@RestClient
	ExtractionProxy proxy;

	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions related to the document identified in the payload")
	@APIResponses({
		@APIResponse(responseCode = "404", description = "An error occurred retrieving extractions"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are extractions")})
	public Response searchExtractions(@QueryParam("term") final String term, @QueryParam("page") final Integer page, @QueryParam("ASKEM_CLASS") String askemClass) {

		Matcher matcher = DOI_VALIDATION_PATTERN.matcher(term);

		Boolean isDoi = matcher.find();

		try {
			XDDResponse<XDDExtractionsResponseOK> response;
			if (isDoi) {
				response = proxy.getExtractions(term, null, page, askemClass);
			} else {
				response = proxy.getExtractions(null, term, page, askemClass);
			}

			if (response.getSuccess() == null || response.getSuccess().getData().isEmpty()) {
				return Response.noContent().build();
			}

			return Response
				.status(Response.Status.OK)
				.entity(response)
				.type(MediaType.APPLICATION_JSON)
				.build();


		} catch (RuntimeException e) {
			log.error("Unable to search in extractions. An error occurred", e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}


	}

	@GET
	@Path("/askem_autocomplete/{term}")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions related to the document identified in the payload")
	@APIResponses({
		@APIResponse(responseCode = "404", description = "An error occurred retrieving auto complete suggestions"),
		@APIResponse(responseCode = "204", description = "Returned if there are no suggestions for the user")
	})
	public Response getAutocomplete(@PathParam("term") String term) {
		try {

			AutoComplete autoComplete = proxy.getAutocomplete(term);
			if (autoComplete.hasNoSuggestions())
				return Response.noContent().build();

			return Response
				.status(Response.Status.OK)
				.entity(autoComplete)
				.type(MediaType.APPLICATION_JSON)
				.build();


		} catch (RuntimeException e) {
			log.error("Unable to autocomplete");
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}
}
