package software.uncharted.terarium.hmiserver.proxies.documentservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.documentservice.autocomplete.AutoComplete;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "xdd-document-service")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface ExtractionProxy {
	@GET
	@Path("/askem/object")
	@LogRestClientTime
	XDDResponse<XDDExtractionsResponseOK> getExtractions(
		@QueryParam("doi") final String doi,
		@QueryParam("query_all") final String queryAll,
		@QueryParam("page") final Integer page,
		@QueryParam("ASKEM_CLASS") String askemClass,
		@QueryParam("include_highlights") String include_highlights,
		@QueryParam("api_key") String apiKey
	);

	@GET
	@Path("askem_autocomplete/{term}")
	@LogRestClientTime
	AutoComplete getAutocomplete(
		@PathParam("term") final String term
	);
}
