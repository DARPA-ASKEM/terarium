package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/concepts")
@Produces(MediaType.APPLICATION_JSON)
public interface ConceptProxy {
	@GET
	Response searchConcept(
		@QueryParam("curie") String curie
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createConcept(
		Concept concept
	);

	@GET
	@Path("/definitions")
	Response searchConceptDefinitions(
		@QueryParam("term") String term,
		@DefaultValue("100") @QueryParam("limit") Integer limit,
		@DefaultValue("0") @QueryParam("offset") Integer offset
	);

	@GET
	@Path("/definitions/{curie}")
	Response getConceptDefinitions(
		@PathParam("curie") String curie
	);

	@GET
	@Path("/{id}")
	Response getConcept(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/{id}")
	Response deleteConcept(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateConcept(
		@PathParam("id") String id,
		Concept concept
	);

	@GET
	@Path("/facets")
	Response searchConceptsUsingFacets(
		@QueryParam("types") List<String> types,
		@QueryParam("curies") List<String> curies
	);
}
