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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import software.uncharted.terarium.hmiserver.models.xdd.Extraction;
import software.uncharted.terarium.hmiserver.models.xdd.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.xdd.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.xdd.ExtractionProxy;
import software.uncharted.terarium.hmiserver.services.xdd.DocumentService;

@Path("/api/xdd/extractions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "XDD Extraction REST Endpoint")
public class ExtractionResource {

		@Inject
		DocumentService documentService;

		@RestClient
		ExtractionProxy proxy;

		@GET
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.APPLICATION_JSON)
		@Tag(name = "Search XDD for extractions related to the document identified in the payload")
		public Response searchExtractions(@QueryParam("doi") String doi) {
			final List<Extraction> extractions = new ArrayList<>();

			// @TODO: validate that a proper doi is given

			try {
				// old implementtion based on http-client service works fine
				// FIXME: use extractionService intead of documentService
				// final List<Extraction> rawExtractions = documentService.getExtractions(doi);

				Object rawExtractionsTemp = proxy.getExtractions(doi);
				var rawExtractions = new ObjectMapper()
					.registerModule(new Jdk8Module())
					.convertValue(rawExtractionsTemp, new TypeReference<XDDResponse<XDDExtractionsResponseOK>>() {});

				if (rawExtractions.success != null && rawExtractions.success.data != null) {
					for (Extraction ex : rawExtractions.success.data) {
						extractions.add(ex);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return Response.ok(extractions).build();
		}
}
