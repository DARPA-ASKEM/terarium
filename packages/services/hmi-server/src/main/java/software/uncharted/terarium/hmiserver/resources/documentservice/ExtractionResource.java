package software.uncharted.terarium.hmiserver.resources.documentservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
@Tag(name = "XDD Extraction REST Endpoint")
@Path("/api/document/extractions")
public class ExtractionResource {


	// source: https://www.crossref.org/blog/dois-and-matching-regular-expressions/
	private static final Pattern DOI_VALIDATION_PATTERN = Pattern.compile("^10.\\d{4,9}\\/[-._;()\\/:A-Z0-9]+$", Pattern.CASE_INSENSITIVE);

	@RestClient
	ExtractionProxy proxy;

	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions related to the document identified in the payload")
	public XDDResponse<XDDExtractionsResponseOK> searchExtractions(@QueryParam("term") final String term, @QueryParam("page") final Integer page, @QueryParam("ASKEM_CLASS") String askemClass) {

		Matcher matcher = DOI_VALIDATION_PATTERN.matcher(term);

		Boolean isDoi = matcher.find();

		if (isDoi) {
			return proxy.getExtractions(term, null, page, askemClass);
		} else {
			return proxy.getExtractions(null, term, page, askemClass);
		}
	}

	@GET
	@Path("/askem_autocomplete/{term}")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Search XDD for extractions related to the document identified in the payload")
	public Response getAutocomplete(@PathParam("term") String term) {
		return proxy.getAutocomplete(term);
	}
}
