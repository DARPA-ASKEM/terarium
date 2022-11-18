package software.uncharted.terarium.hmiserver.resources.xdd;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import software.uncharted.terarium.hmiserver.proxies.xdd.ExtractionProxy;

@Path("/api/xdd/extractions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Extraction REST Endpoint")
public class ExtractionResource {

		@RestClient
		ExtractionProxy proxy;

		@GET
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.APPLICATION_JSON)
		@Tag(name = "Search XDD for extractions related to the document identified in the payload")
		public Response searchExtractions(@QueryParam("doi") String doi) {
			return proxy.getExtractions(doi);
		}
}
